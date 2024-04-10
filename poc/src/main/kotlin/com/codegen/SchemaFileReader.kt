package com.codegen

import java.io.File

class SchemaFileReader(directoryPath: String) {
    private val directory = File(directoryPath)

    fun readDirectory() {
        if (!directory.isDirectory) {
            println("Not a valid directory path.")
        }

        val files = directory.listFiles() ?: return // Get an array of files in the directory

        for (file in files) {
            if (!file.isFile) return
            // Process each file
            val fileName = file.name
            val strings = fileName.split(Regex.fromLiteral("."), 2)
            val fileType = strings[1]
            if (fileType.isNotBlank()) {
                extractSQLFile(fileType, file)
            }
        }
    }

    private fun extractSQLFile(fileType: String, file: File) {
        if ("sql" != fileType) return
        val lines = extractTableNameAndFields(
            file.bufferedReader()
                .readText()
        )

        val parameters = arrayListOf<String>()
        for (line: String in lines) {
            if (line.startsWith("CREATE TABLE")) {
                val entityName = extractTableName(line)
                println("table name = $entityName")
            } else {
                val parameterLines = line.split(",", ",\n").filter { it.isNotBlank() }
                parameterLines.forEach {
                    if (it.isNotBlank()) {
                        val name = extractFieldsAndTypes(it)
                        parameters.add("::${name.first}:${name.second}")
                    } else {
                        println("No parameter found!")
                    }
                }
            }
        }

        parameters.forEach { println("Parameter:: $it") }
    }

    private fun extractTableName(line: String): String {
        val matchResult = matchResult(line)

        return if (matchResult == null) {
            ""
        } else {
            return matchResult.groupValues[1]
        }
    }

    private fun extractFieldsAndTypes(line: String): Pair<String, String> {
        val fieldPattern = "\"(\\w+)\"\\s+(\\w+)"
        val regex = Regex(fieldPattern)
        val matches = regex.findAll(line)
        val isNullable = isNullableField(line)
        val isId = line.contains("GENERATED ALWAYS AS IDENTITY".toRegex())

        if (isId) println("$line :: is an ID!")

        assert(matches.toList().size == 1)

        val matched = matches.iterator().next()

        return Pair(matched.groupValues[1], getKotlinEquivalentType(matched.groupValues[2], isNullable))
    }

    private fun getKotlinEquivalentType(type: String, isNullable: Boolean): String {
        val kotlinType = when {
            type.lowercase().matches("decimal|numeric".toRegex()) -> "BigDecimal"
            type.lowercase().matches("int".toRegex()) -> "Integer"
            type.lowercase().matches("boolean".toRegex()) -> "Boolean"
            type.lowercase().matches("timestamptz".toRegex()) -> "OffsetDateTime"
            type.lowercase().matches("bigint".toRegex()) -> "Long"
            type.lowercase().matches("char\\(\\d+\\)|character\\(\\d+\\)|text".toRegex()) -> "String"
            type.lowercase().matches("uuid".toRegex()) -> "UUID"
            else -> error("Unable to match type for database field $type!")
        }
        return kotlinType + if (isNullable) "?" else ""
    }


    private fun matchResult(line: String): MatchResult? {
        val fields = line.split(" ").filter { it.isNotBlank() }
        val pattern = "\"(.*?)\"".toRegex()

        for (field in fields) {
            val matchResult = pattern.find(field)
            if (matchResult != null) return matchResult
        }

        return null
    }

    private fun extractTableNameAndFields(textBlock: String): List<String> {
        val pattern = """(.*?)\((.*)\)""".toRegex(RegexOption.DOT_MATCHES_ALL)
        val matchResult = pattern.find(textBlock)

        return if (matchResult != null) {
            val firstPart = matchResult.groupValues[1].trim()
            val secondPart = matchResult.groupValues[2].trim()

            println("First Part:")
            println(firstPart)

            println("\nSecond Part:")
            println(secondPart)

            listOf(firstPart, secondPart)
        } else emptyList()
    }

    private fun isNullableField(test: String): Boolean {
        val regex = Regex.fromLiteral("NOT NULL")
        return !test.contains(regex)
    }

//    private fun getFieldDefaultValue(test: String): String {
//
//    }
}


fun main() {
    val filereader = SchemaFileReader("poc/src/main/resources")
    filereader.readDirectory()
}

