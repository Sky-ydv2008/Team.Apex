using System;
using System.Drawing;
using System.IO;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using Microsoft.Win32;

namespace ApexInnovators.App
{
    [ComVisible(true)]
    public class ScriptBridge
    {
        public bool IsApp() { return true; }
        public string GetAppVersion() { return "1.0.1"; }
        public string GetPlatform() { return "Windows"; }
    }

    public class ModernAppForm : Form
    {
        [DllImport("dwmapi.dll")]
        private static extern int DwmSetWindowAttribute(IntPtr hwnd, int attr, ref int attrValue, int attrSize);

        [DllImport("urlmon.dll", CharSet = CharSet.Ansi)]
        private static extern int UrlMkSetSessionOption(int dwOption, string pBuffer, int dwBufferLength, int dwReserved);
        private const int URLMON_OPTION_USERAGENT = 0x10000001;

        private Panel navHeader;
        private Button btnBack;
        private Button btnRefresh;
        private Button btnHome;
        private Label lblTitle;
        private ProgressBar progressBar;
        private WebBrowser browser;

        private readonly string startUrl;
        private readonly bool isAdmin;

        public ModernAppForm(string title, string url, bool admin)
        {
            startUrl = url;
            isAdmin = admin;

            Text = title;
            Width = 1320;
            Height = 860;
            MinimumSize = new Size(960, 640);
            StartPosition = FormStartPosition.CenterScreen;
            BackColor = Color.FromArgb(10, 15, 26); // #0a0f1a

            // Enable DWM Dark Titlebar on Windows 10/11
            EnableDarkTitleBar();

            // Set Custom User-Agent for frontend app mode detection
            string appVariant = isAdmin ? "ApexAdminWindows/1.0.1 ApexAdmin ApexInnovators" : "ApexInnovatorsWindows/1.0.1 ApexInnovators";
            string customUa = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 " + appVariant;
            UrlMkSetSessionOption(URLMON_OPTION_USERAGENT, customUa, customUa.Length, 0);

            InitializeUI();
        }

        private void EnableDarkTitleBar()
        {
            try
            {
                int darkMode = 1;
                DwmSetWindowAttribute(Handle, 20, ref darkMode, sizeof(int)); // DWMWA_USE_IMMERSIVE_DARK_MODE
                DwmSetWindowAttribute(Handle, 19, ref darkMode, sizeof(int));
            }
            catch { }
        }

        private void InitializeUI()
        {
            // Top Navigation Bar
            navHeader = new Panel
            {
                Dock = DockStyle.Top,
                Height = 44,
                BackColor = Color.FromArgb(15, 23, 42), // #0f172a
                Padding = new Padding(8, 6, 8, 6)
            };

            btnBack = CreateNavButton("← Back", 70);
            btnBack.Click += (s, e) => { if (browser.CanGoBack) browser.GoBack(); };

            btnRefresh = CreateNavButton("↻ Refresh", 80);
            btnRefresh.Click += (s, e) => browser.Refresh();

            btnHome = CreateNavButton("⌂ Home", 70);
            btnHome.Click += (s, e) => browser.Navigate(startUrl);

            lblTitle = new Label
            {
                Text = Text,
                ForeColor = Color.FromArgb(226, 232, 240), // #e2e8f0
                Font = new Font("Segoe UI", 10F, FontStyle.Bold),
                AutoSize = true,
                Location = new Point(245, 12)
            };

            navHeader.Controls.Add(btnBack);
            navHeader.Controls.Add(btnRefresh);
            navHeader.Controls.Add(btnHome);
            navHeader.Controls.Add(lblTitle);

            // Progress Bar
            progressBar = new ProgressBar
            {
                Dock = DockStyle.Top,
                Height = 3,
                Style = ProgressBarStyle.Marquee,
                MarqueeAnimationSpeed = 30,
                Visible = true
            };

            // Browser Control
            browser = new WebBrowser
            {
                Dock = DockStyle.Fill,
                ScriptErrorsSuppressed = true,
                IsWebBrowserContextMenuEnabled = true,
                ObjectForScripting = new ScriptBridge()
            };

            browser.Navigating += (s, e) =>
            {
                progressBar.Visible = true;
                btnBack.Enabled = browser.CanGoBack;
            };

            browser.DocumentCompleted += (s, e) =>
            {
                progressBar.Visible = false;
                btnBack.Enabled = browser.CanGoBack;

                try
                {
                    // Inject JavaScript app mode flag
                    if (browser.Document != null)
                    {
                        browser.Document.InvokeScript("eval", new object[] {
                            "window.isApexApp = true; if (typeof applyAppModeDOM === 'function') applyAppModeDOM();"
                        });
                    }
                }
                catch { }
            };

            Controls.Add(browser);
            Controls.Add(progressBar);
            Controls.Add(navHeader);

            Load += (s, e) => browser.Navigate(startUrl);
        }

        private Button CreateNavButton(string text, int width)
        {
            Button btn = new Button
            {
                Text = text,
                Width = width,
                Height = 30,
                FlatStyle = FlatStyle.Flat,
                ForeColor = Color.FromArgb(125, 211, 252), // #7dd3fc
                BackColor = Color.FromArgb(30, 41, 59), // #1e293b
                Font = new Font("Segoe UI", 9F, FontStyle.Regular),
                Margin = new Padding(0, 0, 6, 0),
                Cursor = Cursors.Hand
            };
            btn.FlatAppearance.BorderSize = 1;
            btn.FlatAppearance.BorderColor = Color.FromArgb(51, 65, 85);
            return btn;
        }
    }

    static class Program
    {
        [STAThread]
        static void Main()
        {
            SetBrowserEmulation();
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            string targetUrl = "https://sky-ydv2008.github.io/Team.Apex/index.html";
            Application.Run(new ModernAppForm("Apex Innovators — Student Developer Team", targetUrl, false));
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
