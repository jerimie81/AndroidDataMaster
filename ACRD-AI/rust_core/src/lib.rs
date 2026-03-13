use jni::objects::{JClass, JString};
use jni::sys::jstring;
use jni::JNIEnv;
use reqwest::blocking::Client;
use serde_json::{json, Value};

const GEMINI_URL: &str =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

fn process_prompt(prompt: &str) -> Result<String, String> {
    let api_key = option_env!("GEMINI_API_KEY").ok_or_else(|| {
        "Configuration error: GEMINI_API_KEY was not provided at compile time.".to_string()
    })?;

    let body = json!({
        "contents": [
            {
                "parts": [
                    {
                        "text": prompt
                    }
                ]
            }
        ]
    });

    let client = Client::new();
    let response = client
        .post(format!("{}?key={}", GEMINI_URL, api_key))
        .json(&body)
        .send()
        .map_err(|e| format!("Network request failed: {e}"))?;

    if !response.status().is_success() {
        let status = response.status();
        let error_body = response
            .text()
            .unwrap_or_else(|_| "Unable to read error body".to_string());
        return Err(format!("API error ({status}): {error_body}"));
    }

    let payload: Value = response
        .json()
        .map_err(|e| format!("Failed to parse Gemini response JSON: {e}"))?;

    let text = payload
        .pointer("/candidates/0/content/parts/0/text")
        .and_then(Value::as_str)
        .ok_or_else(|| "Gemini response missing expected text content.".to_string())?;

    Ok(text.to_string())
}

#[no_mangle]
pub extern "system" fn Java_com_jerimie_acrdai_MainActivity_processGeminiPrompt(
    mut env: JNIEnv,
    _class: JClass,
    prompt: JString,
) -> jstring {
    let input = match env.get_string(&prompt) {
        Ok(p) => p.to_string_lossy().into_owned(),
        Err(e) => {
            return env
                .new_string(format!("Input error: {e}"))
                .expect("Failed to build JNI error string")
                .into_raw();
        }
    };

    let output = match process_prompt(&input) {
        Ok(result) => result,
        Err(err) => err,
    };

    env.new_string(output)
        .expect("Failed to create JNI output string")
        .into_raw()
}
