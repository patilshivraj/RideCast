import os
import re

def find_unused_imports(filepath):
    with open(filepath, 'r') as f:
        content = f.read()

    imports = re.findall(r'^import\s+([\w\.]+)', content, re.MULTILINE)
    unused = []
    for imp in imports:
        # Get the class/function name (last part of the import)
        name = imp.split('.')[-1]
        if name == '*':
            continue
        
        # Count occurrences of the name in the file
        # It should be > 1 (1 for the import itself, >1 for usage)
        # We need to match it as a whole word
        pattern = r'\b' + re.escape(name) + r'\b'
        matches = re.findall(pattern, content)
        if len(matches) == 1:
            unused.append(imp)
            
    return unused

for root, dirs, files in os.walk('app/src/main/java'):
    for file in files:
        if file.endswith('.kt'):
            filepath = os.path.join(root, file)
            unused = find_unused_imports(filepath)
            if unused:
                print(f"{filepath}: {unused}")
