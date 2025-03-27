import os
import subprocess
import sys

def generate_cpg(file_path, output_dir):
    """
    Generate a CPG for a single C file.
    Joern version used is Version: 4.0.233
    """
    base_name = os.path.basename(file_path)
    file_name, _ = os.path.splitext(base_name)
    output_cpg = os.path.join(output_dir, f"{file_name}_cpg.bin")
    
    # Command using joern-parse:
    command = ["joern-parse", "--output", output_cpg, file_path]
    
    try:
        subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, check=True)
        print(f"[+] Generated CPG for {file_path} at {output_cpg}")
    except subprocess.CalledProcessError as e:
        print(f"[-] Error generating CPG for {file_path}:")
        print(e.stderr)
        sys.exit(1)
    return output_cpg

def run_metrics_on_cpg(cpg_path, script_path):
    """
    Run the Joern metrics script on the provided CPG.
    """
    # Ensure the script exists
    if not os.path.exists(script_path):
        print(f"[-] Error: Metrics script not found at {script_path}")
        return None

    # Command to run Joern with metrics script
    command = ["joern", "--script", script_path, cpg_path]
    
    try:
        result = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, check=True)
        print(f"[+] Metrics extracted for {cpg_path}")
        return result.stdout
    except subprocess.CalledProcessError as e:
        print(f"[-] Error running metrics on {cpg_path}:")
        print(e.stderr)
        return None

if __name__ == '__main__':
    # Directory with C files
    input_dir = "data/test_files"
    
    # Directory where generated CPGs will be stored (using mounted volume)
    cpg_dir = "data/test_output"
    
    # Path to Joern query script that extracts metrics
    metrics_script = "/app/metrics.sc"
    
    # Create the CPG output directory if it doesn't exist
    os.makedirs(cpg_dir, exist_ok=True)
    
    # Ensure the metrics script exists
    if not os.path.exists(metrics_script):
        print(f"[-] Error: Metrics script not found at {metrics_script}")
        sys.exit(1)
    
    # Iterate over each .cpp file in the input directory
    for file in os.listdir(input_dir):
        if file.endswith(".cpp"):
            file_path = os.path.join(input_dir, file)
            # Generate the CPG for this file
            cpg_file = generate_cpg(file_path, cpg_dir)
            # Run the metrics script on the generated CPG
            metrics_output = run_metrics_on_cpg(cpg_file, metrics_script)
            if metrics_output:
                print(metrics_output)

