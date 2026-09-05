using System;
using System.Drawing;
using System.IO;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using Microsoft.Win32;

namespace ApexInnovators.AdminApp
{
    [ComVisible(true)]
    public class ScriptBridge
    {
        public bool IsApp() { return true; }
        public string GetAppVersion() { return "1.0.0"; }
        public string GetPlatform() { return "Windows"; }
        public bool IsAdmin() { return true; }
    }

    static class Program
    {
        [DllImport("urlmon.dll", CharSet = CharSet.Ansi)]
        private static extern int UrlMkSetSessionOption(int dwOption, string pBuffer, int dwBufferLength, int dwReserved);
        private const int URLMON_OPTION_USERAGENT = 0x10000001;

        [STAThread]
        static void Main()
        {
            SetBrowserEmulation();
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            string customUa = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 ApexAdminWindows/1.0.0 ApexAdmin ApexInnovators";
            UrlMkSetSessionOption(URLMON_OPTION_USERAGENT, customUa, customUa.Length, 0);

            Form form = new Form
            {
                Text = "Apex Admin — Core Team & Platform Management",
                Width = 1300,
                Height = 850,
                MinimumSize = new Size(900, 600),
                StartPosition = FormStartPosition.CenterScreen,
                BackColor = Color.FromArgb(10, 15, 26),
                Icon = SystemIcons.Shield
            };

            WebBrowser browser = new WebBrowser
            {
                Dock = DockStyle.Fill,
                ScriptErrorsSuppressed = true,
                IsWebBrowserContextMenuEnabled = true,
                ObjectForScripting = new ScriptBridge()
            };

            form.Controls.Add(browser);
            form.Load += (s, e) =>
            {
                browser.Navigate("https://sky-ydv2008.github.io/Team.Apex/admin/dashboard.html");
            };

            Application.Run(form);
        }

        private static void SetBrowserEmulation()
        {
            try
            {
                string appName = Path.GetFileName(Application.ExecutablePath);
                using (RegistryKey key = Registry.CurrentUser.CreateSubKey(@"Software\Microsoft\Internet Explorer\Main\FeatureControl\FEATURE_BROWSER_EMULATION"))
                {
                    if (key != null)
                    {
                        key.SetValue(appName, 11001, RegistryValueKind.DWord);
                    }
                }
            }
            catch { }
        }
    }
}
