rm -dfr /bin/willy
mkdir -p /bin/willy
mkdir -p /willy
apt update
apt upgrade -y
apt install openjdk-17-jre-headless -y