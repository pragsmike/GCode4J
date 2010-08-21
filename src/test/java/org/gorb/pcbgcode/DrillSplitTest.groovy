package org.gorb.pcbgcode;

import java.io.File;
import java.io.FileNotFoundException;

import junit.framework.TestCase;

import static org.junit.Assert.*

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager
import org.junit.After;
import org.junit.Test


public class DrillSplitTest extends TestCase 
{
	File destDir = new File("/tmp/dest")
	DrillSplit drillSplit = new DrillSplit()
	
	@After
	void tearDown() {
		try {FileUtils.forceDelete(destDir) } catch (FileNotFoundException e) {}
	}
	
	void testInitialConfiguration() throws Exception {
		assert drillSplit.preambleName == "preamble.nc"
		assert drillSplit.postambleName == "postamble.nc"
	}
	
	void testSplitWritesFiles() throws Exception {
		drillSplit.preambleName = "test-preamble"
		drillSplit.postambleName = "test-postamble"

		try {FileUtils.forceDelete(destDir) } catch (FileNotFoundException e) {}

		destDir.mkdirs();
		def input = new ClasspathResourceManager().getReader("all-drill.nc")
		drillSplit.doSplit(destDir, "all-drill.nc", input)

		assert 5 == destDir.list().length

		checkFiles(destDir)
	}

	void testSplitFile() throws Exception {
		drillSplit.preambleName = "test-preamble"
		drillSplit.postambleName = "test-postamble"

		try {FileUtils.forceDelete(destDir) } catch (FileNotFoundException e) {}

		destDir.mkdirs();
		def outFile = new File(destDir, "all-drill.nc")
		def input = new ClasspathResourceManager().getReader("all-drill.nc")
		def output = new FileOutputStream(outFile)
		IOUtils.copy input, output
		input.close()
		output.close()

		drillSplit.splitFile outFile
		
		assert 6 == destDir.list().length

		checkFiles(destDir)
	}

	void checkFiles(destDir) {
		assert new File(destDir, "all-drill-T01.nc").exists()
		assert new File(destDir, "all-drill-T02.nc").exists()
		assert new File(destDir, "all-drill-T03.nc").exists()
		assert new File(destDir, "all-drill-T04.nc").exists()
		assert new File(destDir, "all-drill-T05.nc").exists()
		
		checkFile("all-drill-T01.nc", "( M06 T01  ; 0.0320 )", 103 )
		checkFile("all-drill-T02.nc", "( M06 T02  ; 0.0394 )", 19 )
		checkFile("all-drill-T03.nc", "( M06 T03  ; 0.0400 )", 25 )
		checkFile("all-drill-T04.nc", "( M06 T04  ; 0.0520 )", 22 )
		checkFile("all-drill-T05.nc", "( M06 T05  ; 0.1102 )", 21 )
	}
	
	void checkFile(fileName, expectedFirstLine, expectedSize) {		
		def content = new File(destDir, fileName).readLines()
		assert content[0] == "(Preamble.)"
		assert content[1] == expectedFirstLine
		assert content[-1] == "(Postamble.)"
		assert content.size() == expectedSize
	}
	
	void testPreamble() {
		drillSplit.preambleName = "test-preamble"

		assert drillSplit.preamble.trim() == "(Preamble.)"		
	}
	void testPostamble() {
		drillSplit.postambleName = "test-postamble"

		assert drillSplit.postamble.trim() == "(Postamble.)"
	}
	
	void testWriteSectionFile() {
		drillSplit.preambleName = "test-preamble"
		drillSplit.postambleName = "test-postamble"
		
		def sec = ["(This)", "(is)", "(section)"]
		StringWriter writer = new StringWriter();
		drillSplit.writeSectionFile(writer, sec)
		def lines = writer.toString().readLines()
		assert lines.size() == 5
		assert lines[0] == "(Preamble.)"
		assert lines[1] == "(This)"
		assert lines[2] == "(is)"
		assert lines[3] == "(section)"
		assert lines[4] == "(Postamble.)"
	}

	void testExtractSection() throws Exception {
		def lines = new ClasspathResourceManager().getReader("all-drill.nc").readLines()
		
		def sec = drillSplit.extractSection("T01", lines)
		
		assert "( M06 T01  ; 0.0320 )" == sec[0]
		assert "G00 Z0.1000 " == sec[1]
		assert "M05" == sec[-1]
		assert "G01 Z-0.0700 F8.00  " == sec[-3]
		assert sec.size() == 101
	}
	
	void testGetTools() throws Exception {
		def lines = [
			"(Generated bottom outlines, bottom drill, )",
			"(Unit of measure: inch)",
			"( Tool|       Size       |  Min Sub |  Max Sub |   Count )",
			"( T01  0.813mm 0.0320in 0.0000in 0.0000in )",
			"( T02  1.000mm 0.0394in 0.0000in 0.0000in )",
			"( T03  1.016mm 0.0400in 0.0000in 0.0000in )",
			"( T04  1.321mm 0.0520in 0.0000in 0.0000in )",
			"( T05  2.800mm 0.1102in 0.0000in 0.0000in )"
		]
		checkGetTools(lines)
	}
	void testGetTools2() throws Exception {
		def input = new ClasspathResourceManager().getReader("all-drill.nc")
		checkGetTools(input.readLines())
	}
	
	void checkGetTools(lines) {
		def tools = drillSplit.getTools(lines)
		assert tools != null
		assert 5 == tools.size()
		assert "T01" == tools[0]
		assert "T02" == tools[1]
		assert "T03" == tools[2]
		assert "T04" == tools[3]
		assert "T05" == tools[4]
	}
	
	void testNextName() throws Exception {
		assert "all-drill-T01.nc" == drillSplit.nextName("all-drill.nc", "T01")
		assert "a-T01.nc" == drillSplit.nextName("a.nc", "T01")
		assert "a-T01.q" == drillSplit.nextName("a.q", "T01")
	}
}
