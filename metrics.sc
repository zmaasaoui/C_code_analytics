import java.io._

// Attempt to retrieve the file name from the CPG's File node (fallback to "unknown_file")
val fileNameOpt = cpg.file.headOption.map(_.name)
val fileName    = fileNameOpt.getOrElse("unknown_file")

// Define the output directory and file path
val outputDir = new File("/app/data")
if (!outputDir.exists()) {
  outputDir.mkdirs()
}
val csvFile = new File(outputDir, "metrics_output.csv")

// We open the CSV in append mode
val appendMode = true
val bw = new BufferedWriter(new FileWriter(csvFile, appendMode))

// Write header if file is empty
if (!csvFile.exists() || csvFile.length() == 0) {
  bw.write("file_name,functions,globalVariables,localVariables,arrays,libCalls,ifConditions,switch,controlStructures,loops\n")
}

// 1. Count of switch statements
val switchCount = cpg.controlStructure.filter(cs => cs.code.trim.startsWith("switch")).size

// 2. Count of local variables
val localVarCount = cpg.local.size

// 3. Count of global variables
val globalVarCount = cpg
  .namespaceBlock
  .nameExact("<global>")
  .astChildren
  .label("VARIABLE_DECLARATION") 
  .size

// 4. Count of arrays
val arrayCount = cpg.local.filter(_.code.contains("[")).size

// 5. Count of lib calls
val libCallsCount = cpg.call.filter { call =>
  call.code.contains("printf") ||
  call.code.contains("malloc") ||
  call.code.contains("free")
}.size

// 6. Count of if conditions
val ifConditionCount = cpg.controlStructure.filter(_.controlStructureType == "IF").size

// 7. Count of functions
val functionCount = cpg.method.size

// 8. Count of all control structures
val controlStructureCount = cpg.controlStructure.size

// 9. Count of loops
val loopCount = cpg.controlStructure.filter { cs =>
  cs.controlStructureType == "FOR" ||
  cs.controlStructureType == "WHILE" ||
  cs.controlStructureType == "DO"
}.size

// Write metrics to CSV
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
  s"$loopCount\n"           // loops
)

bw.close()

// Print metrics to stdout as well
println(s"Metrics for $fileName:")
println(s"Functions: $functionCount")
println(s"Global Variables: $globalVarCount")
println(s"Local Variables: $localVarCount")
println(s"Arrays: $arrayCount")
println(s"Library Calls: $libCallsCount")
println(s"If Conditions: $ifConditionCount")
println(s"Switch Statements: $switchCount")
println(s"Control Structures: $controlStructureCount")
println(s"Loops: $loopCount")
