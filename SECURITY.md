# Security Policy

## Supported Versions

We are committed to maintaining security updates for the following versions:

| Version        | Supported       |
|----------------|-----------------|
| v1.0.0         | (Latest)        |
| 0.0.1-SNAPSHOT | Initial version |

**Note:** As this is an educational project, we support only the latest development version. For production use, please ensure you're using the most recent release.

## Reporting a Vulnerability

We take security vulnerabilities seriously. If you discover a security issue, please report it responsibly:

### How to Report

1. **Do NOT** open a public GitHub issue for security vulnerabilities
2. **Email:** Contact the project maintainer (see [Team](https://github.com/samehinttech/diving-courses-trips-logbook-project/wiki/Team) page)
3. Include the following information:
   - Description of the vulnerability
   - Steps to reproduce the issue
   - Potential impact
   - Suggested fix (if available)

### What to Expect

- **Initial Response**: Within 48-72 hours
- **Status Updates**: Every 5-7 days until resolved
- **Resolution Time**: Depends on severity
  - Critical: 1-3 days
  - High: 1-2 weeks
  - Medium: 2-4 weeks
  - Low: Best effort basis

### Our Commitment

- We will acknowledge your report promptly
- We will keep you informed of our progress
- We will credit you for responsible disclosure (if desired)
- We will publish a security advisory once the issue is resolved

### Security Best Practices

When deploying this application:
- Keep all dependencies updated
- Use strong credentials for database and email services
- Enable HTTPS in production
- Regularly review security advisories for dependencies
- Follow the principle of the least privilege for user accounts

## Known Security Considerations

This is an **educational project** developed for the Internet Technology module at FHNW:
- Uses H2 as an in-memory database (not suitable for production without modifications)
- May contain educational shortcuts not appropriate for production use
- Requires security hardening before production deployment

## Dependency Security

We actively monitor and update dependencies to address known vulnerabilities:
- Spring Framework 6.2.10 (CVE-2025-41242 fixed)
- commons-lang3 3.18.0 (CVE-2025-48924 fixed)
- logback-core 1.5.19 (CVE-2025-11226 fixed)

---

**For general questions or feature requests**, please use [GitHub Issues](https://github.com/samehinttech/diving-courses-trips-logbook-project/issues).
