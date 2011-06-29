package org.genedb.crawl.bam;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;



import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
//import org.codehaus.jackson.type.TypeReference;
import org.genedb.crawl.json.JsonIzer;
import org.genedb.crawl.model.Alignment;
import org.genedb.crawl.model.AlignmentSequenceAlias;
import org.genedb.crawl.model.Alignments;

import org.genedb.crawl.model.Variant;

public class BioDataFileStoreInitializer {
	
	private static Logger logger = Logger.getLogger(BioDataFileStoreInitializer.class);
	
	private JsonIzer jsonIzer = new JsonIzer();
	
	private BioDataFileStore<Alignment> alignmentStore = new BioDataFileStore<Alignment>();
	private BioDataFileStore<Variant> variantStore = new BioDataFileStore<Variant>();
	
	public BioDataFileStore<Alignment> getAlignments() {
		return alignmentStore;
	}
	
	public BioDataFileStore<Variant> getVariants() {
		return variantStore;
	}
	
	public void setAlignmentFiles(File alignmentFile) throws JsonParseException, JsonMappingException, IOException {
		
		logger.info(String.format("Alignment file : %s" , alignmentFile));
		
		if (alignmentFile == null) {
			return;
		}
		
		if (alignmentFile.isFile() == false) {
			return;
		}
		
		logger.info("making jsons");
		
		Alignments store = (Alignments) jsonIzer.fromJson(alignmentFile, Alignments.class);
		
		
		Map<String, String> sequences = new HashMap<String,String>();
		
		if (store.sequences != null) {
			for (AlignmentSequenceAlias alias : store.sequences) {
				sequences.put(alias.reference, alias.alignment);
			}
		}
		
		alignmentStore = new BioDataFileStore<Alignment>(store.alignments, sequences);
		
		if (store.variants != null) {
			variantStore = new BioDataFileStore<Variant>(store.variants, sequences);
		}
		
	}
	
	
	
}
