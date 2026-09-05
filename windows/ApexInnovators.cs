using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

namespace ApexInnovators.App
{
    static class Program
    {
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            string targetUrl = "https://sky-ydv2008.github.io/Team.Apex/index.html";
            string edgePath = FindEdgeExecutable();

            if (!string.IsNullOrEmpty(edgePath) && File.Exists(edgePath))
            {
                string userDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "ApexInnovatorsWindowsApp");
                Directory.CreateDirectory(userDir);

                string customUa = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 ApexInnovatorsWindows/1.0.1 ApexInnovators";
                string args = string.Format("--app=\"{0}\" --user-data-dir=\"{1}\" --user-agent=\"{2}\" --window-size=1320,860", targetUrl, userDir, customUa);

                ProcessStartInfo psi = new ProcessStartInfo
                {
                    FileName = edgePath,
                    Arguments = args,
                    UseShellExecute = false
                };

                try
                {
                    Process.Start(psi);
                    return;
                }
                catch { }
            }

            // Fallback: Open in default system browser
            try
            {
                Process.Start(new ProcessStartInfo
                {
                    FileName = targetUrl,
                    UseShellExecute = true
                });
            }
            catch (Exception ex)
            {
                MessageBox.Show("Could not launch application: " + ex.Message, "Apex Innovators", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        private static string FindEdgeExecutable()
        {
            string[] searchPaths = new string[]
            {
                @"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe",
                @"C:\Program Files\Microsoft\Edge\Application\msedge.exe",
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), @"Microsoft\Edge\Application\msedge.exe"),
                Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles), @"Microsoft\Edge\Application\msedge.exe")
            };

            foreach (string p in searchPaths)
            {
                if (File.Exists(p)) return p;
            }

            // Search EdgeCore or EdgeWebView directories
            try
            {
                string baseDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86), "Microsoft");
                if (Directory.Exists(baseDir))
                {
                    string[] matches = Directory.GetFiles(baseDir, "msedge.exe", SearchOption.AllDirectories);
                    if (matches.Length > 0) return matches[0];
                }
            }
            catch { }

            return null;
        }
    }
}
