package org.genedb.crawl.business.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.genedb.crawl.CrawlErrorType;
import org.genedb.crawl.CrawlException;
import org.genedb.crawl.model.MappedOrganism;
import org.genedb.crawl.model.interfaces.Organisms;
import org.springframework.stereotype.Component;

@Component
public class OrganismsQueries extends Base implements Organisms {

	@Override
	public List<MappedOrganism> list()  throws CrawlException {
		
		IndexReader reader = repo.luceneIndexReader();
		IndexSearcher searcher = new IndexSearcher(reader);
		
		// prefix query with empty string, returns docs with all non empty values for organism.common_name
		PrefixQuery pq = new PrefixQuery(new Term("organism.common_name", "")); 
		
		List<MappedOrganism> organisms = new ArrayList<MappedOrganism>();
		
		try {
			
			TopDocs td = searcher.search(pq, reader.maxDoc());
			
			for (ScoreDoc sd : td.scoreDocs) {
				Document d = reader.document(sd.doc);
				MappedOrganism o = generateFromDocument(d); 
				organisms.add(o);
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new CrawlException("Could not execute query.", CrawlErrorType.QUERY_FAILURE);
		}
		
		return organisms;
	}
	
	private String getFieldIfPresent(Document d, String fieldName) {
		
		try {
			return d.getField(fieldName).stringValue();
		} catch (NullPointerException npe) {
			logger.warn(npe);
			return null;
		} 
		
	}
	
	private MappedOrganism generateFromDocument(Document d) {
		MappedOrganism o = new MappedOrganism ();
		o.common_name = getFieldIfPresent(d, "organism.common_name");
		o.genus = getFieldIfPresent(d, "organism.genus");
		o.species = getFieldIfPresent(d, "organism.species");
		o.taxonID = getFieldIfPresent(d, "organism.taxon_id");
		o.translation_table = getFieldIfPresent(d, "organism.tranlation_table");
		o.ID = getFieldIfPresent(d, "organism.id");
		o.name = o.genus + " " + o.species;
		return o;
	}
	
	private MappedOrganism search(String term, String value) throws CrawlException {
		IndexReader reader = repo.luceneIndexReader();
		IndexSearcher searcher = new IndexSearcher(reader);
		
		PrefixQuery pq = new PrefixQuery(new Term(term, value));
		
		try {
			TopDocs td = searcher.search(pq, reader.maxDoc());
			
			if (td.scoreDocs.length > 0) {
				Document d = reader.document(td.scoreDocs[0].doc);
				MappedOrganism o = generateFromDocument(d); 
				return o;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new CrawlException("Could not execute query.", CrawlErrorType.QUERY_FAILURE);
		}
		
		return null;
	}

	@Override
	public MappedOrganism getByID(int ID) throws CrawlException {
		return search("organism.id", String.valueOf(ID));
	}

	@Override
	public MappedOrganism getByCommonName(String commonName)
			throws CrawlException {
		return search("organism.common_name", commonName);
	}

	@Override
	public MappedOrganism getByTaxonID(String taxonID) throws CrawlException {
		return search("organism.taxon_id", taxonID);
	}
	

}
