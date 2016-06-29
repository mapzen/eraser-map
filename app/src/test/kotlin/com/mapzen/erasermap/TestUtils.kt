package com.mapzen.erasermap

import com.google.common.io.Files
import com.mapzen.valhalla.Instruction
import org.json.JSONObject
import java.io.File

class TestUtils {

  companion object {

    fun getInstruction(name: String):Instruction {
      return Instruction(JSONObject(getFixture(name + ".instruction")))
    }

    private fun getFixture(name: String): String {
      val basedir = System.getProperty("user.dir")
      val file = File(basedir + "/src/test/fixtures/" + name)
      var fixture = ""
      try {
        fixture = Files.toString(file, com.google.common.base.Charsets.UTF_8)
      } catch (e: Exception) {
        fixture = "not found"
      }

      return fixture
    }
  }

}