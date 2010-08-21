package org.gorb.pcbgcode

import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager;

class DrillSplit {
	
	def preambleName = "preamble.nc"
	def postambleName = "postamble.nc"
	
	static void main(String[] args) {
		if (args.length < 1) {
			println "Usage: drillSplit fileToSplit"
		} else {
			new DrillSplit().splitFile new File(args[0])
		}
	}
	
	void splitFile(inputFile) {
		def input = new FileReader(inputFile)
		doSplit(inputFile.getParentFile(), inputFile.getName(), input)
	}
	
	void doSplit(File destDir, String prefix, Reader input) {
		def lines = input.readLines()
		input.close()
		def tools = getTools(lines)
		tools.each { tool ->
			def file = new File(destDir, nextName(prefix, tool))
			def sec = extractSection(tool, lines)
			def writer = new FileWriter(file)
			writeSectionFile(writer, sec)
			writer.close()
		}
	}
	String nextName(prefix, tool) {
		def match = (prefix =~ /^(.*)(\.[^.]+)$/)
		def base = match[0][1]
		def ext = match[0][2]
		"${base}-${tool}${ext}"
	}
	def getTools(lines) {
		lines = lines.findAll { it =~ /.*\( T\d\d .*/}
		lines.collect { (it =~ /.*\( (T\d\d).*/)[0][1]}
	}
	def extractSection(tool, lines) {
		def startIndex = lines.findIndexOf { it =~ /M06 ${tool}/}
		def rest = lines[startIndex..-1]
		def endIndex = rest.findIndexOf { it =~ /M05/}
		
		def startLine = rest[0]
		def out = [ "( ${startLine})"]
		
		out.addAll rest[1..endIndex]
		out
	}
	
	def getPreamble() {
		new ClasspathResourceManager().getReader(preambleName).text
	}
	def getPostamble() {
		new ClasspathResourceManager().getReader(postambleName).text
	}
	def writeSectionFile(writer, sec) {
		def w = new PrintWriter(writer)
		w.println(preamble.trim())
		sec.each { w.println it }
		w.println(postamble.trim())
	}
}
