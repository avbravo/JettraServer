import json
import os
import base64

def process_css(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            # Safely replace double quotes with single quotes for CSS
            content = content.replace('"', "'")
            return content
    except Exception as e:
        print(f"Error reading {filepath}: {e}")
        return ""

def process_js(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            # Base64 encode the JS so we don't have to worry about escaping quotes
            encoded = base64.b64encode(content.encode('utf-8')).decode('utf-8')
            
            # Create a small script that decodes and injects the JS
            # Note: We strictly use single quotes here to avoid double quotes!
            wrapper = (
                "var b64 = '" + encoded + "';\n"
                "var decoded = atob(b64);\n"
                "var s = document.createElement('script');\n"
                "s.textContent = decoded;\n"
                "document.head.appendChild(s);\n"
            )
            return wrapper
    except Exception as e:
        print(f"Error reading {filepath}: {e}")
        return ""

def main():
    adminlte_dir = "../../admin-lte-v4.2.0"
    css_path = os.path.join(adminlte_dir, "dist/css/adminlte.min.css")
    js_path = os.path.join(adminlte_dir, "dist/js/adminlte.min.js")
    
    css_content = process_css(css_path)
    js_content = process_js(js_path)
    
    theme_data = {
        "name": "Traditional",
        "primary": "#0d6efd",
        "secondary": "#6c757d",
        "background": "#f4f6f9",
        "surface": "#ffffff",
        "onPrimary": "#ffffff",
        "onSurface": "#212529",
        "buttonStyle": "border: none; border-radius: 4px; padding: 8px 16px; font-weight: 400; cursor: pointer; transition: color .15s ease-in-out,background-color .15s ease-in-out,border-color .15s ease-in-out,box-shadow .15s ease-in-out; background-color: #0d6efd; color: #ffffff;",
        "cardStyle": "border-radius: .25rem; box-shadow: 0 0 1px rgba(0,0,0,.125), 0 1px 3px rgba(0,0,0,.2); padding: 1.25rem; background-color: #ffffff; border: 0 solid rgba(0,0,0,.125); color: #212529;",
        "containerStyle": "padding: 15px; border-radius: .25rem; background: #f4f6f9; color: #212529;",
        "textStyle": "font-family: 'Source Sans Pro', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; font-size: 1rem; color: #212529; line-height: 1.5;",
        "customCss": css_content,
        "customJs": js_content
    }
    
    with open("src/main/resources/META-INF/theme.json", "w", encoding="utf-8") as f:
        json.dump(theme_data, f, indent=2)
        
    print("theme.json generated successfully.")

if __name__ == "__main__":
    main()
