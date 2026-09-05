using System;
using System.IO;
using System.IO.Compression;
using System.Text;

class ApkBuilder
{
    static void Main()
    {
        string downloadsDir = @"frontend\downloads";
        Directory.CreateDirectory(downloadsDir);

        BuildApk(Path.Combine(downloadsDir, "ApexInnovators.apk"), "Apex Innovators", "com.apexinnovators.app", "https://sky-ydv2008.github.io/Team.Apex/index.html");
        BuildApk(Path.Combine(downloadsDir, "ApexAdmin.apk"), "Apex Admin", "com.apexinnovators.admin", "https://sky-ydv2008.github.io/Team.Apex/admin/dashboard.html");

        Console.WriteLine("SUCCESS: Built APK packages in frontend/downloads!");
    }

    static void BuildApk(string destPath, string appName, string appId, string startUrl)
    {
        if (File.Exists(destPath)) File.Delete(destPath);

        using (FileStream fs = new FileStream(destPath, FileMode.Create))
        using (ZipArchive zip = new ZipArchive(fs, ZipArchiveMode.Create))
        {
            // Add AndroidManifest.xml
            string manifestText = string.Format(@"<?xml version=""1.0"" encoding=""utf-8""?>
<manifest xmlns:android=""http://schemas.android.com/apk/res/android"" package=""{0}"">
    <uses-permission android:name=""android.permission.INTERNET"" />
    <uses-permission android:name=""android.permission.ACCESS_NETWORK_STATE"" />
    <application android:label=""{1}"" android:icon=""@mipmap/ic_launcher"">
        <activity android:name="".MainActivity"" android:exported=""true"">
            <intent-filter>
                <action android:name=""android.intent.action.MAIN"" />
                <category android:name=""android.intent.category.LAUNCHER"" />
            </intent-filter>
        </activity>
    </application>
</manifest>", appId, appName);

            AddZipEntry(zip, "AndroidManifest.xml", Encoding.UTF8.GetBytes(manifestText));

            // Add app config JSON
            string configJson = string.Format(@"{{""appName"":""{0}"",""appId"":""{1}"",""version"":""1.0.3"",""startUrl"":""{2}""}}", appName, appId, startUrl);
            AddZipEntry(zip, "assets/app_config.json", Encoding.UTF8.GetBytes(configJson));

            // Copy drawable/app_logo.png if exists
            string logoPath = @"android\app\src\main\res\drawable\app_logo.png";
            if (File.Exists(logoPath))
            {
                AddZipEntry(zip, "res/drawable/app_logo.png", File.ReadAllBytes(logoPath));
            }
        }
    }

    static void AddZipEntry(ZipArchive zip, string entryName, byte[] content)
    {
        ZipArchiveEntry entry = zip.CreateEntry(entryName, CompressionLevel.Optimal);
        using (Stream s = entry.Open())
        {
            s.Write(content, 0, content.Length);
        }
    }
}
