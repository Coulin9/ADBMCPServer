class AdbmcpServer < Formula
  desc "MCP server exposing ADB commands as tools for AI agents"
  homepage "https://github.com/Coulin9/ADBMCPServer"
  url "https://github.com/Coulin9/ADBMCPServer/releases/download/v1.0.1/ADBMCPServer-1.0.1-all.jar"
  sha256 "dae13815b8f26c11d47cc465d942066abd13d4101536b4f19e068d9caf8b7bf2"
  license "MIT"

  depends_on "openjdk@21"

  def install
    libexec.install "ADBMCPServer-#{version}-all.jar" => "adbmcp-server.jar"

    (bin/"adbmcp-server").write <<~EOS
      #!/bin/bash
      if ! command -v adb &>/dev/null; then
        echo "Error: adb not found in PATH" >&2
        echo "Install it with: brew install --cask android-platform-tools" >&2
        exit 1
      fi
      export JAVA_HOME="#{Formula["openjdk@21"].opt_prefix}"
      exec "${JAVA_HOME}/bin/java" -jar "#{libexec}/adbmcp-server.jar" "$@"
    EOS
  end

  def caveats
    <<~EOS
      ADB (Android Debug Bridge) is required but not installed automatically.
      Install it with:
        brew install --cask android-platform-tools
    EOS
  end

  test do
    output = shell_output("#{bin}/adbmcp-server --help 2>&1", 1)
    assert_match(/adb|error|usage/i, output)
  end
end
