# For Source Only
function ensure_node() {
    NVM_DIR="${HOME}/.nvm"
    echo "check nvm.sh..."
    if [[ ! -s "$NVM_DIR/nvm.sh" ]]; then
        echo "download nvm install.sh from github..."
        curl -o- https://raw.githubusercontent.com/creationix/nvm/v0.30.1/install.sh | bash
    fi
    [[ -s "$NVM_DIR/nvm.sh" ]] && . "$NVM_DIR/nvm.sh"
    echo "check nvm cmd..."
    if type nvm &> /dev/null; then
        echo "use node v4.2..."
        nvm install 4.2
        nvm use 4.2
    fi
    echo "echo node & npm version..."
    node -v
    npm -v
}