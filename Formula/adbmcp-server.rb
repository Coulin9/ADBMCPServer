class AdbmcpServer < Formula
  desc "MCP server exposing ADB commands as tools for AI agents"
  homepage "https://github.com/Coulin9/ADBMCPServer"
  url "https://github.com/Coulin9/ADBMCPServer/releases/download/v1.0.2/ADBMCPServer-1.0.2-all.jar"
  sha256 "3a069d0f41922865c02fdb2886637a106990f863e587492762aea8d103bf295f"
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
