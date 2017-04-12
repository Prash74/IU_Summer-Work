package fingerprints;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.BitSet;
import java.util.List;

import org.openscience.cdk.ChemFile;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.ExtendedFingerprinter;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.fragment.MurckoFragmenter;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.INChIPlainTextReader;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.io.iterator.IteratingSMILESReader;
import org.openscience.cdk.qsar.descriptors.molecular.RuleOfFiveDescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.TPSADescriptor;
import org.openscience.cdk.qsar.descriptors.molecular.WeightDescriptor;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;




public class ScaffoldGenerator {

	/*public static void main(String[] args) throws IOException, CDKException {
		ScaffoldGenerator sg = new ScaffoldGenerator();
		
		
		sg.processChemicalEntities(args[0], args[1], Boolean.valueOf(args[2]));

	}*/
	
	/**
	 * Process n number of molecule entities (usually 25K molecules) but could also be a single molecule in the case of smiles (.smi) input.
	 * 
	 * @param molecules molecules in the form of sdf or smi
	 * @param sdfOrSmi flag indicating file format (eg sdf, but also sometimes smi-smiles)
	 * @param singleFragmentOnly flag indicating only want 1 molecular scaffold (as opposed to many potentially small scaffolds)
	 * @return
	 * @throws IOException
	 * @throws CDKException
	 */
	public static StringBuilder processChemicalEntities(String molecules, String sdfOrSmi, boolean singleFragmentOnly) throws IOException, CDKException {
		//FileReader fileReader = new FileReader(fileStr);
		
		StringReader sr = new StringReader(molecules);
		//SMILESReader reader = new SMILESReader(fileReader);
		IChemObjectBuilder ico = DefaultChemObjectBuilder.getInstance();
		
		StringBuilder resultssb = new StringBuilder();
		
		//System.err.println("START OF INPUT SPLIT SDF:");
		//System.err.print(molecules);
		//System.err.println("END OF INPUT SPLIT SDF");
		//pw = new PrintWriter(fileStr + "scaffold.descriptors");
		
		if (sdfOrSmi.equals("smi")) {
			IteratingSMILESReader reader = new IteratingSMILESReader(sr,ico);		
		int molcnt=0;
		while (reader.hasNext()) {
			//System.err.println("molcnt:"+molcnt++);
			IAtomContainer mol = reader.next();			
			resultssb.append(generateFragmentFingerprint(mol,singleFragmentOnly)+"\n");
		}
		reader.close();		
		}
		
		else {
			IteratingSDFReader reader = new IteratingSDFReader(sr,ico);		
			int molcnt=0;
			//System.err.println("TRYIIINNG TO ITERATE IN SDF FILE molcnt:"+molcnt++);
			while (reader.hasNext()) {
				//System.err.println("TRYIIINNG TO GENENERATE molcnt:"+molcnt++);
				IAtomContainer mol = reader.next();
				resultssb.append(generateFragmentFingerprint(mol,singleFragmentOnly) +"\n");
			}
			reader.close();		
		}
		return resultssb;
	}
	
	
	
	public static String generateFragmentFingerprint(IAtomContainer molecule, boolean singleFragmentOnly) {
		MurckoFragmenter mf = new MurckoFragmenter(singleFragmentOnly,5);
		String fp="";
		try {
			
			//System.err.println(molecule.getProperty("cdk:Title") +"|");
			String title  = (String)molecule.getProperty("cdk:Title");
			if (molecule.getAtomCount() > 300) {
				System.err.println(title + ": exceeds" + molecule.getAtomCount());
				return (title + ": exceeds" + molecule.getAtomCount());
			}
			//if (title.contains("nucleic acid") || title.contains("Trecovirsen sodium")) {
			//	System.err.println("skipping:" + title);	
			//	return;
			//}
			
			mf.generateFragments(molecule);
			
			if (mf.getFragments().length == 0) {
				System.err.println("hasNoFragments:" + title);
				return "hasNoFragments:" + title;
			}
			//System.out.println("-------------------------------------------");
			
			
			for (String str : mf.getFrameworks()){
				//System.err.println(str + "|"+ molecule.getProperty("cdk:Title") +"|");
				//InChIGeneratorFactory.getInstance().getInChIGenerator(molecule).getInchi();
				SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
				IAtomContainer temp = sp.parseSmiles(str);
				//String inchiscaff = InChIGeneratorFactory.getInstance().getInChIGenerator(temp).getInchi();
				//System.out.println(inchiscaff + "|" + molecule.getProperty("cdk:Title"));
				
				
				ExtendedFingerprinter fprinter = new ExtendedFingerprinter();
				IBitFingerprint fingerprint = fprinter.getBitFingerprint(temp);
				
				
				//System.out.print(inchiscaff + "|" + str);
				
				//System.out.println("|"+ title + "|" +toString(fingerprint.asBitSet()));
				fp =  title + "|" +toString(fingerprint.asBitSet());
				
			}
			
			//System.err.println("finished:"+molecule.getProperty("cdk:Title") +"|");
			
			//System.out.println();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}
		
		return fp;
	}
	
	private static String toString(BitSet bs) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < bs.size(); ++i) {
			if (bs.get(i))
				sb.append("1");
			else 
				sb.append("0");
			//System.out.println(bs.get(i));
		}
		return sb.toString();
        //return Long.toString(bs.toLongArray()[0], 2);
    }

}
