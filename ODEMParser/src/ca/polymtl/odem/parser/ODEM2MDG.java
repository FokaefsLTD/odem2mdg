package ca.polymtl.odem.parser;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.polymtl.jaxb.schema.odem.ODEM;
import ca.polymtl.jaxb.schema.odem.ODEM.Context.Container.Namespace;
import ca.polymtl.jaxb.schema.odem.ODEM.Context.Container.Namespace.Type;
import ca.polymtl.jaxb.schema.odem.ODEM.Context.Container.Namespace.Type.Dependencies.DependsOn;

public class ODEM2MDG {

	private HashMap<String,ArrayList<String>> classDependencies;
	
	public ODEM2MDG() {
		classDependencies = new HashMap<String,ArrayList<String>>();
	}
	
	public void readDocument(String filePath) {
		
		try {
			File inputFile = new File(filePath);
			
			JAXBContext jaxbContext = JAXBContext.newInstance(ODEM.class);
			
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			ODEM odem = (ODEM)jaxbUnmarshaller.unmarshal(inputFile);
			
			List<Namespace> namespaces = odem.getContext().getContainer().getNamespace();
			for(Namespace namespace : namespaces) {
				List<Type> types = namespace.getType();
				for(Type type : types) {
					List<DependsOn> dependsOn = type.getDependencies().getDependsOn();
					ArrayList<String> dependencies = new ArrayList<String>();
					for(DependsOn depends : dependsOn) {
						if(isLocal(depends.getName())) {
							dependencies.add(depends.getName());
						}
					}
					if (!dependencies.isEmpty()) {
						classDependencies.put(type.getName(), dependencies);
					}
				}
			}
			
			System.out.println(odem);

		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean isLocal(String name) {
		if(name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("com.sun.") || name.startsWith("org.xml.sax.") || name.startsWith("org.omg.") || name.startsWith("org.w3c.dom.")) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public void writeMDG(String outputFilepath) {
		File outputFile = new File(outputFilepath);
		try {
			BufferedWriter o = new BufferedWriter(new FileWriter(outputFile));
			for(String type : classDependencies.keySet()) {
				for(String dependency : classDependencies.get(type)) {
					o.write(type+" "+dependency);
					o.newLine();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ODEM2MDG parser = new ODEM2MDG();
		parser.readDocument(args[0]);
		parser.writeMDG(args[1]);
		System.out.println("Finish!");
	}
	
}
