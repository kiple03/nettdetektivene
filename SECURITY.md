# Security Policy

Thank you for helping keep this project and its users safe.  
This document explains how to report security vulnerabilities responsibly.

## Supported Versions

Security updates are generally provided for the following versions:

| Version                    | Supported          |
|---------------------------|--------------------|
| `main` / `master` branch  | ✅ Supported       |
| Latest release            | ✅ Supported       |
| Older releases            | ⚠️ Best-effort only; please update to the latest version |

If in doubt, report the issue anyway.

## Reporting a Vulnerability

If you believe you have found a security vulnerability in this project:

1. **Do not open a public issue.**
2. Instead, contact the maintainers privately via **one** of the following methods:

   - **Email:** `ingvev@stud.ntnu.no`
   - **GitHub Security Advisory:**  
     Go to **Security → Advisories → Report a vulnerability** in this repository.

3. Use the subject line:  
   `Security: [Short description of the issue]`

4. **Provide as much detail as possible**, including:
   - A clear description of the issue and its potential impact
   - Steps to reproduce (PoC, minimal code, or commands)
   - Affected versions / environments
   - Any logs, screenshots, or crash reports (redact sensitive data)
   - Suggested remediation or workaround, if you have one

If you are unsure whether an issue is a security vulnerability, please **report it privately anyway**. We will assess it.

## What to Expect

After you report a vulnerability:

1. **Acknowledgment**  
   We will acknowledge your report within **[365 business days]**.

2. **Initial Assessment**  
   We will review the issue and:
   - Confirm whether we can reproduce it;
   - Assess severity and impact;
   - Determine affected versions.

3. **Remediation Plan**  
   We will work on a fix and prepare:
   - A patch or configuration change;
   - A release plan for patched versions;
   - Security advisory text (if needed).

4. **Coordinated Disclosure**  
   We aim for coordinated disclosure. We prefer:
   - To release fixes and advisories **before** public disclosure;
   - To agree on a disclosure timeline with you (commonly **30–90 days**, depending on severity and complexity).

We will keep you informed about:
- Whether we consider the report valid;
- Expected timelines for fixes or releases;
- Any changes to the disclosure plan.

## Scope and Guidelines

When researching and reporting:

- **Allowed / In Scope**
  - Testing this project’s code, configurations, and deployment as documented.
  - Local testing in your own environment.
  - Non-destructive tests that do not harm data or availability.

- **Not Allowed / Out of Scope**
  - Any actions that violate applicable laws.
  - Accessing, modifying, or deleting data you do not own.
  - Denial-of-service attacks or anything that degrades service for other users.
  - Social engineering of maintainers or users.
  - Testing against systems or infrastructure not owned by this project.

Please make a good-faith effort to avoid:
- Privacy violations;
- Destruction of data;
- Interruptions to our or users’ services.

If you accidentally access more data than necessary, **stop immediately**, do not store the data, and include the minimal necessary details in your report.

## Public Disclosure

We kindly request that you:

- Do not publicly disclose the vulnerability (e.g., in GitHub issues, blog posts, or social media) **until we have had a reasonable opportunity to investigate and release a fix**;
- Coordinate timing and technical details of any disclosure with us;
- Reference the official advisory or release notes once they are available.

We may credit you for the discovery (with your permission) in:
- Release notes;
- Security advisories;
- Hall of Fame / acknowledgments section (if applicable).

If you prefer to remain anonymous, let us know in your report.

## Questions

If you have questions about this policy (not a vulnerability report), you may:

- Open a regular GitHub issue with the label `question`, or
- Contact us at: `ingvev@stud.ntnu.no`
