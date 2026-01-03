#!/usr/bin/env python3
"""Callback for git-filter-repo to clean Jenkinsfile."""

def callback(blob, metadata):
    """Process each blob in the repository."""
    if blob.path == b'Jenkinsfile':
        import re
        content = blob.data
        
        # Replace PAT URL
        content = re.sub(
            rb'https://charbelba:github_pat_[^@]*@github\.com/charbelba/ERP\.git',
            b'https://github.com/MichaelRouhana/FYP-Backend.git',
            content
        )
        
        # Replace master with main
        content = re.sub(
            rb"git branch:\s*['\"]master['\"]",
            b"git branch: 'main'",
            content
        )
        
        # Add credentialsId if needed
        if b'git branch:' in content and b'credentialsId' not in content:
            lines = content.split(b'\n')
            new_lines = []
            for line in lines:
                new_lines.append(line)
                if b'git branch:' in line and b'credentialsId' not in line:
                    indent = len(line) - len(line.lstrip())
                    new_lines.append(b' ' * indent + b"credentialsId: 'github_credentials',")
            content = b'\n'.join(new_lines)
        
        blob.data = content

