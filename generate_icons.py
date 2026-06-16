import os
from PIL import Image

image_path = "/home/eliezer/.gemini/antigravity-ide/brain/5406b6ea-7f04-48b0-8b42-6de2a87d23c9/media__1781591848588.png"
base_dir = "app/src/main/res"

sizes = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192
}

try:
    img = Image.open(image_path)
    for density, size in sizes.items():
        dir_path = os.path.join(base_dir, f"mipmap-{density}")
        os.makedirs(dir_path, exist_ok=True)
        
        resized_img = img.resize((size, size), Image.Resampling.LANCZOS)
        
        # Save standard icon
        resized_img.save(os.path.join(dir_path, "ic_launcher.png"))
        
        # Save round icon (same image for simplicity, as the uploaded image has a rounded shape)
        resized_img.save(os.path.join(dir_path, "ic_launcher_round.png"))
        
    print("Icons generated successfully.")
except Exception as e:
    print(f"Error: {e}")
