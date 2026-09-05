using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;

class LogoConverter
{
    static void Main()
    {
        string[] candidates = new string[]
        {
            @"C:\Users\ODIN\.omp\agent\blobs\628f81e799212e42393836ce1501c9d5378c5e6b479637643a02b4d523296f1d.jpg",
            @"C:\Users\ODIN\.omp\agent\blobs\88146af5f0403e8bef0f14baf463f95665a9045ba4a2f9542062cbed7afd54bb.jpg"
        };

        string srcPath = null;
        foreach (var c in candidates)
        {
            if (File.Exists(c))
            {
                using (Bitmap b = new Bitmap(c))
                {
                    Console.WriteLine("Candidate: " + c + " (" + b.Width + "x" + b.Height + ")");
                    if (b.Width == 1024 && b.Height == 1024)
                    {
                        srcPath = c;
                        break;
                    }
                }
            }
        }

        if (srcPath == null)
        {
            Console.WriteLine("No 1024x1024 logo image found!");
            return;
        }

        using (Bitmap src = new Bitmap(srcPath))
        {
            Console.WriteLine("Using 1024x1024 logo image: " + srcPath);

            string resDir = @"android\app\src\main\res";
            Directory.CreateDirectory(resDir + @"\drawable");
            Directory.CreateDirectory(@"frontend\img");

            SaveResized(src, 1024, 1024, resDir + @"\drawable\app_logo.png");
            SaveResized(src, 512, 512, resDir + @"\drawable\ic_launcher_foreground.png");
            SaveResized(src, 512, 512, @"frontend\logo.png");
            SaveResized(src, 128, 128, @"frontend\favicon.png");

            var mipmaps = new[]
            {
                new { dir = "mipmap-mdpi", size = 48, fg = 108 },
                new { dir = "mipmap-hdpi", size = 72, fg = 162 },
                new { dir = "mipmap-xhdpi", size = 96, fg = 216 },
                new { dir = "mipmap-xxhdpi", size = 144, fg = 324 },
                new { dir = "mipmap-xxxhdpi", size = 192, fg = 432 },
            };

            foreach (var m in mipmaps)
            {
                string targetDir = Path.Combine(resDir, m.dir);
                Directory.CreateDirectory(targetDir);
                SaveResized(src, m.size, m.size, Path.Combine(targetDir, "ic_launcher.png"));
                SaveResized(src, m.size, m.size, Path.Combine(targetDir, "ic_launcher_round.png"));
                SaveResized(src, m.fg, m.fg, Path.Combine(targetDir, "ic_launcher_foreground.png"));
            }

            SaveIco(src, @"windows\app.ico");
            SaveIco(src, @"windows\admin.ico");
            Console.WriteLine("SUCCESS: All Android, Windows, and Web icons generated from the 1024x1024 TEAM APEX INNOVATORS logo!");
        }
    }

    static void SaveResized(Bitmap src, int width, int height, string destPath)
    {
        using (Bitmap dest = new Bitmap(width, height))
        {
            using (Graphics g = Graphics.FromImage(dest))
            {
                g.InterpolationMode = InterpolationMode.HighQualityBicubic;
                g.SmoothingMode = SmoothingMode.HighQuality;
                g.PixelOffsetMode = PixelOffsetMode.HighQuality;
                g.CompositingQuality = CompositingQuality.HighQuality;
                g.Clear(Color.Transparent);
                g.DrawImage(src, 0, 0, width, height);
            }
            dest.Save(destPath, ImageFormat.Png);
        }
    }

    static void SaveIco(Bitmap src, string destPath)
    {
        using (Bitmap iconBmp = new Bitmap(256, 256))
        {
            using (Graphics g = Graphics.FromImage(iconBmp))
            {
                g.InterpolationMode = InterpolationMode.HighQualityBicubic;
                g.SmoothingMode = SmoothingMode.HighQuality;
                g.DrawImage(src, 0, 0, 256, 256);
            }

            using (FileStream fs = new FileStream(destPath, FileMode.Create))
            {
                fs.WriteByte(0); fs.WriteByte(0);
                fs.WriteByte(1); fs.WriteByte(0);
                fs.WriteByte(1); fs.WriteByte(0);

                using (MemoryStream ms = new MemoryStream())
                {
                    iconBmp.Save(ms, ImageFormat.Png);
                    byte[] pngBytes = ms.ToArray();

                    fs.WriteByte(0);
                    fs.WriteByte(0);
                    fs.WriteByte(0);
                    fs.WriteByte(0);
                    fs.WriteByte(1); fs.WriteByte(0);
                    fs.WriteByte(32); fs.WriteByte(0);

                    uint size = (uint)pngBytes.Length;
                    fs.Write(BitConverter.GetBytes(size), 0, 4);
                    uint offset = 6 + 16;
                    fs.Write(BitConverter.GetBytes(offset), 0, 4);
                    fs.Write(pngBytes, 0, pngBytes.Length);
                }
            }
        }
    }
}
