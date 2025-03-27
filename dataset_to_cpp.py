import os

def c_file_to_cpp(files_directory):
    if not os.path.exists(files_directory):
        print(f"Directory '{files_directory}' does not exist.")
        return

    for file in os.listdir(files_directory):
        if file.endswith(".c"):
            file_path = os.path.join(files_directory, file)
            new_file_path = file_path.replace(".c", ".cpp")
            os.rename(file_path, new_file_path)
            print(f"Renamed: {file_path} -> {new_file_path}")

if __name__ == "__main__":
    files_directory = "data/test_files"
    c_file_to_cpp(files_directory)
