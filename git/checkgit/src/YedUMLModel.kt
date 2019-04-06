import org.dom4j.Document
import org.dom4j.Element
import org.dom4j.io.SAXReader
import java.io.File
import java.io.StringReader
import javax.swing.text.ElementIterator
import javax.swing.text.html.HTML
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLEditorKit

class YedUMLModel (
        val sourceFile : File
) {

    val methods = MethodMap()
    val types = UMLTypes()

    fun readYed() {
        val doc: Document =
            sourceFile.bufferedReader().use {
                val reader = SAXReader()
                reader.read(it)
            }
        methods.addFromYed(doc)
        types.addFromYed(doc)
    }

    fun dump() {
        val sortedMethods = methods.values.sortedBy { it.canonicalName }
        println("Classes and methods in ${sourceFile.name}:")
        for (t : UMLTypes.Type in types.typesByName.values.sortedBy{it.name}) {
            print("    ${t.name}")
            if (t.supertypes.isEmpty()) {
                println("  (no supertypes)")
            } else {
                print("  (Supertype(s):  ")
                var first = true;
                for (s in t.supertypes.sortedBy { it.name }) {
                    if (first) {
                        first = false
                    } else {
                        print(", ")
                    }
                    print(s.name)
                }
                println(")")
            }
            for (m in sortedMethods) {
                if (m.appearsIn.contains(t.name))  {
                    println("        ${m.names}")
                }
            }

        }
        println()
    }

}

class UMLTypes {

    /** Name in lower case */
    val typesByName = mutableMapOf<String, Type>()

    class Type(
            val name : String,
            var canonicalizedName : String,
            val isInterface : Boolean,
            val found : Boolean = true
    ) {
        var supertypes = mutableListOf<Type>()
        val subtypes = mutableListOf<Type>()

        val leaves get() : List<Type> = addLeaves(mutableListOf<Type>())

        private fun addLeaves(leaves: MutableList<Type>) : MutableList<Type> {
            for (t in subtypes) {
                if (t.subtypes.size == 0) {
                    leaves.add(t)
                } else {
                    t.addLeaves(leaves)
                }
            }
            return leaves
        }

        fun isAssignableTo(other: Type?) : Boolean {
            if (other == this) {
                return true
            } else if (other == null) {
                return false
            }
            for (s in supertypes) {
                if (s.isAssignableTo(other)) {
                    return true
                }
            }
            return false
        }
    }

    fun addFromYed(doc: Document) {
        val typesByID = addTypes(doc)
        findSupertypes(doc, typesByID)
    }

    fun getTypeStrict(name: String) : Type? = typesByName[canonicalizeName(name)]

    fun getType(vararg names: String, condition: ((n : Type) -> Boolean)? = null) : Type {
        for (n in names) {
            val result = getTypeStrict(n)
            if (result != null) {
                if (condition == null || condition(result)) {
                    return result
                }
            }
        }
        val result = Type(names.first(), canonicalizeName(names.first()), false, false)
        typesByName[result.canonicalizedName] = result

        return result
    }

    fun canonicalizeName(name: String, underscoreReplacement: String = "") : String {
        var s = name.replace(" ", "").replace("_", underscoreReplacement).toLowerCase()
        if (s.startsWith("<<interface>>")) {
            s = s.drop("<<interface>>".length)
        } else if (s.endsWith("<<interface>>")) {
            s = s.dropLast("<<interface>>".length)
        } else if (s.startsWith("<interface>")) {
            s = s.drop("<interface>".length)
        } else if (s.endsWith("<interface>")) {
            s = s.dropLast("<interface>".length)
        }
        return s
    }

    private fun addTypes(doc: Document) : Map<String, Type> {
        val typesByID = mutableMapOf<String, Type>()
        for (c in doc.selectNodes("//y:UMLClassNode")) {
            val el = c as Element
            var className = el.elements().first { it.name == "NodeLabel" && it.textTrim != "" }.textTrim.replace(" ", "")
            val umlElement = el.elements().first { it.name == "UML" }
            val isInterface = umlElement.attributeValue("stereotype").trim().toLowerCase().contains("interface")
            val id = el.parent.parent.attributeValue("id")
            val canon = canonicalizeName(className, "_")
            val type = Type(className, canon, isInterface)
            typesByID[id] = type
            typesByName[canon] = type
        }
        //
        // Now scan for "_" in the name, but only use the "_" version if there's no conflict
        //
        for (key in ArrayList<String>(typesByName.keys)) {
            if (key.contains('_')) {
                val ck = canonicalizeName(key)
                if (!typesByName.containsKey(ck)) {
                    val value = typesByName.remove(key)!!
                    value.canonicalizedName = ck;
                    typesByName[ck] = value
                }
            }
        }
        return typesByID
    }

    private fun findSupertypes(doc: Document, typesByID: Map<String, Type>) {
        for (arrows in doc.selectNodes("//y:Arrows")) {
            if ((arrows as Element).attributeValue("target") == "white_delta") {
                val edge = arrows.parent.parent.parent
                val subtype = typesByID[edge.attributeValue("source")]
                val supertype = typesByID[edge.attributeValue("target")]
                subtype!!.supertypes.add(supertype!!)
                supertype.subtypes.add(subtype)
            } else if ((arrows).attributeValue("source") == "white_delta") {
                val edge = arrows.parent.parent.parent
                val subtype = typesByID[edge.attributeValue("target")]
                val supertype = typesByID[edge.attributeValue("source")]
                subtype!!.supertypes.add(supertype!!)
                supertype.subtypes.add(subtype)
            }
        }
    }
}
/**
 * Represents a set of methods in user code, identified by name without regard to case.
 */
class MethodMap : HashMap<String, MethodMap.Method>() {

    class Method (
            val canonicalName : String
    ){
        val names = mutableSetOf<String>()
        val appearsIn = mutableSetOf<String>()
    }


    fun getMethod(name : String) : Method {
        val nameLower = name.toLowerCase().replaceAfter(':', "").split(' ').last().replaceAfter('(', "")
        // In case they put " : type" or "type methodname" or "method()"
        var method = this[nameLower]
        if (method == null) {
            method = Method(nameLower)
            this[nameLower] = method
        }
        method.names.add(name)
        return method
    }

    fun addFromYed(doc : Document) {
        var htmlKit: HTMLEditorKit? = null;
        for (methodLabel in doc.selectNodes("//y:MethodLabel")) {
            var className = methodLabel.parent.parent.elements()
                    .firstOrNull { it.name == "NodeLabel" && it.textTrim != "" }?.textTrim
            if (className != null) {
                if (className.startsWith("<<interface>>")) {
                    className = className.drop("<<interface>>".length).trim()
                }
                if (methodLabel.text.startsWith("<html>", ignoreCase=true)) {
                    if (htmlKit == null) {
                        htmlKit = HTMLEditorKit()
                    }
                    val methodDoc: HTMLDocument  = htmlKit.createDefaultDocument() as HTMLDocument
                    htmlKit.read(StringReader(methodLabel.text), methodDoc, 0)
                    val iter = ElementIterator(methodDoc)
                    while (true) {
                        val e = iter.next()
                        if (e == null) {
                            break
                        }
                        if (e.getName() == "content") {
                            val method = e.document.getText(e.startOffset, e.endOffset-e.startOffset)
                            processMethod(method, className)
                        }
                    }
                } else {
                    for (method in methodLabel.text.split('\n')) {
                        processMethod(method, className)
                    }
                }
            }
        }
    }

    private fun processMethod(methodRaw : String, className : String) {
        var method = methodRaw.trim();
        while (true) {
            if (method.startsWith('-') || method.startsWith('+')) {
                method = method.drop(1)
            } else {
                break;
            }
        }
        method = method.substringBefore("(").substringBefore(":").trim()
        if (method != "") {
            getMethod(method).appearsIn.add(className)
        }
    }
}
