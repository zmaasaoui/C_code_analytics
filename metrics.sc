import java.io._

// Attempt to retrieve the file name from the CPG's File node (fallback to "unknown_file")
val fileNameOpt = cpg.file.headOption.map(_.name)
val fileName    = fileNameOpt.getOrElse("unknown_file")

// Define the output CSV file path (this will be created in the working directory)
val csvFile = new File("metrics_output.csv")

// We open the CSV in append mode. If you want to overwrite each time, use `false`.
val appendMode = true
val bw = new BufferedWriter(new FileWriter(csvFile, appendMode))

// --- Optionally write a header row if the file does not exist or is empty ---
if (!csvFile.exists() || csvFile.length() == 0) {
  bw.write("file_name,functions,globalVariables,localVariables,arrays,libCalls,ifConditions,switch,controlStructures,loops\n")
}

// 1. Count of switch statements (using code startsWith "switch")
val switchCount = cpg.controlStructure.filter(cs => cs.code.trim.startsWith("switch")).size

// 2. Count of local variables
val localVarCount = cpg.local.size

// 3. Count of global variables
//    In older Joern versions, top-level globals appear under the `<global>` namespace block,
//    and are labeled "VARIABLE_DECLARATION". Hence we filter on `label("VARIABLE_DECLARATION")`.
val globalVarCount = cpg
  .namespaceBlock
  .nameExact("<global>")
  .astChildren
  .label("VARIABLE_DECLARATION") 
  .size

// 4. Count of arrays (simple heuristic: local's code has "[")
val arrayCount = cpg.local.filter(_.code.contains("[")).size

// 5. Count of (lib) calls (example: printf, malloc, free)
val libCallsCount = cpg.call.filter { call =>
  call.code.contains("printf") ||
  call.code.contains("malloc") ||
  call.code.contains("free")
}.size

// 6. Count of if conditions (using controlStructureType == "IF")
val ifConditionCount = cpg.controlStructure.filter(_.controlStructureType == "IF").size

// 7. Count of functions (methods)
val functionCount = cpg.method.size

// 8. Count of all control structures (if, for, while, switch, etc.)
val controlStructureCount = cpg.controlStructure.size

// (Optional) 9. Count of loops (FOR, WHILE, DO)
val loopCount = cpg.controlStructure.filter { cs =>
  cs.controlStructureType == "FOR" ||
  cs.controlStructureType == "WHILE" ||
  cs.controlStructureType == "DO"
}.size

// --- Write all metrics in one CSV line ---
bw.write(
  s"$fileName," +           // file_name
  s"$functionCount," +      // functions
  s"$globalVarCount," +     // globalVariables
  s"$localVarCount," +      // localVariables
  s"$arrayCount," +         // arrays
  s"$libCallsCount," +      // libCalls
  s"$ifConditionCount," +   // ifConditions
  s"$switchCount," +        // switch
  s"$controlStructureCount," + // controlStructures
  s"$loopCount\n"           // loops (optional)
)

bw.close()

println(s"Metrics written to: ${csvFile.getAbsolutePath}")
