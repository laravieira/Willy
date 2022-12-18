rename 's/willy-\d{1,3}\.\d{1,3}\.\d{1,3}-jar-with-dependencies.jar/willy.jar/' willy-*-jar-with-dependencies.jar
cat > /etc/systemd/system/willy.service <<EOF
[Unit]
Description=Willy service
After=network.target
StartLimitIntervalSec=0

[Service]
Type=simple
Restart=always
RestartSec=1
User=ubuntu
ExecStart=java --add-opens java.base/java.lang=ALL-UNNAMED -jar /bin/willy/willy-0.16.1-jar-with-dependencies.jar
RuntimeDirectory=/willy

[Install]
WantedBy=multi-user.target
EOF
