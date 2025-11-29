# Project Setup

To set up this project, please follow these steps:

1.  **Clone this project:**
    ```bash
    git clone https://github.com/SkyVikXII/android_java_aichatbot.git
    ```
	
2.  **Add Firebase Database to the project:**
    You can do this via Android Studio or by manually adding your `google-services.json` file to `./app/google-services.json`.

3.  **Configure Firebase for API Keys:**
    Add the following field to your Firebase database. This allows loading and saving API keys more securely than hardcoding them.

    ```json
    {
      "system": {
        "endpoint": [
          {
            "api_KEY": "your api key",
            "default": true,
            "endpoint_url": "https://openrouter.ai/api/v1/chat/completions",
            "name": "open router"
          }
        ]
      }
    }
    ```
4.  **Project Firebase structure:**
	```json
	{
	  "system": {
		"endpoint": [
		  {},
		  {},
		  {}
		]
	  },
	  "users": {
		"userid1": {
		},
		"userid2": {
		},
		"userid3": {
		},
	  }
	}
    ```