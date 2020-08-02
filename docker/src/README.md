# PV Stats - deployment manual

### Deployment steps
1. If services are running (update deployment), stop them first by executing the command `docker-compose down`.
2. Run install script to load required Docker images: `install/install.sh`
3. Set required environment variables in the `.env` file.
4. Replace placeholders in the `resources/maria/init/init.sql` file to match those from `.env` file.
These placeholders cannot be replaced automatically (at least at the moment).
5. Set proper domain names in the `config/httpd/pv-stats.conf` files.
6. Start all services: `docker-compose up -d`.

### Obtaining SSL certificate (Let's encrypt)
ENV file requires providing the location of cartificate and private key files.
Those files can be obtained using the following instructions.

1. Install `certboot`.
2. Obtain standard certificate using `certbot certonly --standalone`. This command requires 
setting up a temporary web server in order to verify domain so *PV Stats* services must be stopped first.
3. Certificate and private key files should be now available at `/etc/letsencrypt/live/<domain_name>`.

**Note:** obtaining wildcard certificate is not supported for all DNS registrars. Refer to
[certbot docs](https://certbot.eff.org/docs/using.html#dns-plugins) for more info.