# PV Stats - deployment manual

### Deployment steps
1. If services are running (update deployment), stop them first by executing the command `docker-compose down`.
2. Run install script to load required Docker images: `install/install.sh`
3. Set required environment variables in the `.env` file.
4. Run `scripts/update_resources.sh` to process resources before lanuching th deployment. This step is required
after each change of the `.env` file.
5. Set proper domain names in the `config/httpd/pv-stats.conf` files.
6. Start all services: `docker-compose up -d`.

### Updating

1. Stop all services: `docker-compose down` 
2. Unpack new archive and replace all contents except the following files and directories:
    - `data/`
    - `config/`
    - `.env` - new items may have been added so contents of this file should be compared with new new version
3. Trigger resources update: `scripts/update_resources.sh`.
4. Start all services: `docker-compose up -d`.



// todo: if mariadb init script is changed it has to be executed manually!!!!!



### Obtaining SSL certificate (Let's encrypt)
ENV file requires providing the location of cartificate and private key files.
Those files can be obtained using the following instructions.

1. Install `certboot`.
2. Obtain standard certificate using `certbot certonly --standalone`. This command requires 
setting up a temporary web server in order to verify domain so *PV Stats* services must be stopped first.
3. Certificate and private key files should be now available at `/etc/letsencrypt/live/<domain_name>`.

**Note:** obtaining wildcard certificate is not supported for all DNS registrars. Refer to
[certbot docs](https://certbot.eff.org/docs/using.html#dns-plugins) for more info.