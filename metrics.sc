// import java.io._

// // Attempt to retrieve the file name from the CPG's File node (fallback to "unknown_file")
// val fileNameOpt = cpg.file.headOption.map(_.name)
// val fileName = fileNameOpt.getOrElse("unknown_file")

// // Define the output CSV file path (this will be created in the working directory)
// val csvFile = new File("metrics_output.csv")

// // Open the file in append mode (set the second parameter to false if you wish to overwrite)
// val bw = new BufferedWriter(new FileWriter(csvFile, true))

// // 1. Count of control structures
// val controlStructureCount = cpg.controlStructure.l.size
// bw.write(s"${fileName},controlStructures,${controlStructureCount}\n")

// // 2. Count of functions (methods)
// val functionCount = cpg.method.l.size
// bw.write(s"${fileName},functions,${functionCount}\n")

// // 3. Count of if conditions
// val ifConditionCount = cpg.controlStructure.filter(_.controlStructureType == "IF").l.size
// bw.write(s"${fileName},ifConditions,${ifConditionCount}\n")

// // 4. Count of loops (for, while, do)
// val loopCount = cpg.controlStructure.filter(cs =>
//   cs.controlStructureType == "FOR" || cs.controlStructureType == "WHILE" || cs.controlStructureType == "DO"
// ).l.size
// bw.write(s"${fileName},loops,${loopCount}\n")

// // 5. Count of libc calls (for example, by checking for common libc function names)
// val libcCallsCount = cpg.call.filter(call =>
//   call.code.contains("printf") || call.code.contains("malloc") || call.code.contains("free")
// ).l.size
// bw.write(s"${fileName},libcCalls,${libcCallsCount}\n")

// // 6. Count of arrays (assuming array declarations include a '[' in their code)
// val arrayCount = cpg.local.filter(_.code.contains("[")).l.size
// bw.write(s"${fileName},arrays,${arrayCount}\n")

// // 7. Count of global variables
// val globalVarCount = cpg.local.filter(local => local.method.name.l.contains("<global>")).l.size
// bw.write(s"${fileName},globalVariables,${globalVarCount}\n")

// // 8. Count of 'switch'
// val switchCount = cpg.controlStructure.filter(cs => cs.code.trim.startsWith("switch")).l.size
// bw.write(s"${fileName},switch,${switchCount}\n")


// bw.close()

// println(s"Metrics written to: ${csvFile.getAbsolutePath}")


import java.io._

// Attempt to retrieve the file name from the CPG's File node (fallback to "unknown_file")
val fileNameOpt = cpg.file.headOption.map(_.name)
val fileName    = fileNameOpt.getOrElse("unknown_file")

// Define the output CSV file path (this will be created in the working directory)
val csvFile = new File("metrics_output.csv")

// We will open the CSV in append mode. If you want to overwrite each time, use `false`.
val appendMode = true
val bw = new BufferedWriter(new FileWriter(csvFile, appendMode))

// --- Optionally write a header row if the file does not exist or is empty ---
if (!csvFile.exists() || csvFile.length() == 0) {
  bw.write("file_name,functions,globalVariables,arrays,libCalls,ifConditions,switch,controlStructures\n")
}

// 1. Count of switch statements
val switchCount = cpg.controlStructure.filter(cs => cs.code.trim.startsWith("switch")).size

// 2. Count of global variables
//    Checking for local variables whose parent method has name "<global>" is one approach:
val globalVarCount = cpg.local.filter(local => local.method.name.l.contains("<global>")).size

// 3. Count of arrays
//    (Assuming an array declaration is indicated by a `[` in the local node's code)
val arrayCount = cpg.local.filter(_.code.contains("[")).size

// 4. Count of (lib) calls
//    (Here, we look for any call that contains certain library functions like printf/malloc/free)
val libCallsCount = cpg.call.filter(call =>
  call.code.contains("printf") || call.code.contains("malloc") || call.code.contains("free")
).size

// 5. Count of if conditions
val ifConditionCount = cpg.controlStructure.filter(_.controlStructureType == "IF").size

// 6. Count of functions (methods)
val functionCount = cpg.method.size

// 7. Count of control structures (all kinds)
val controlStructureCount = cpg.controlStructure.size

// (Optional) Count of loops, if you still want them, not included in final CSV columns:
// val loopCount = cpg.controlStructure.filter(cs => 
//   cs.controlStructureType == "FOR" || 
//   cs.controlStructureType == "WHILE" || 
//   cs.controlStructureType == "DO"
// ).size

// --- Write all metrics in one line per file ---
bw.write(s"$fileName," +               // file_name
         s"$functionCount," +          // functions
         s"$globalVarCount," +         // globalVariables
         s"$arrayCount," +             // arrays
         s"$libCallsCount," +          // libCalls
         s"$ifConditionCount," +       // ifConditions
         s"$switchCount," +            // switch
         s"$controlStructureCount\n")  // controlStructures

bw.close()

println(s"Metrics written to: ${csvFile.getAbsolutePath}")
