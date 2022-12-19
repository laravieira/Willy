cp -r /opt/codedeploy-agent/deployment-root/$DEPLOYMENT_GROUP_ID/$DEPLOYMENT_ID/deployment-archive/* /bin/willy
rename 's|/bin/willy/willy-\d{1,3}\.\d{1,3}\.\d{1,3}-jar-with-dependencies.jar|/bin/willy/willy.jar|' /bin/willy/willy-*-jar-with-dependencies.jar
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
ExecStart=java --add-opens java.base/java.lang=ALL-UNNAMED -jar /bin/willy/willy.jar
RuntimeDirectory=/willy

[Install]
WantedBy=multi-user.target
EOF
