using System;
using System.Diagnostics;
using System.IO;

class TestApp
{
    static void Main()
    {
        string edgePath = @"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe";
        string url = "https://sky-ydv2008.github.io/Team.Apex/index.html";
        string userDir = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData), "ApexTestApp");

        ProcessStartInfo psi = new ProcessStartInfo
        {
            FileName = edgePath,
            Arguments = string.Format("--app=\"{0}\" --user-data-dir=\"{1}\" --user-agent=\"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36 ApexInnovatorsWindows/1.0.1 ApexInnovators\"", url, userDir),
            UseShellExecute = false
        };

        Process p = Process.Start(psi);
        Console.WriteLine("Started Edge App Mode PID: " + p.Id);
    }
}
