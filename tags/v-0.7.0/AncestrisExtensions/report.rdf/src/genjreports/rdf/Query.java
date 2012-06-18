package genjreports.rdf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;
import genjreports.rdf.ReportRdf.Extension;

public class Query {

	private static final String LS = System.getProperty("line.separator");

	public static void main(final String[] args) throws IOException {

		if (args.length == 0) {
			System.err.println("stdout: name space prefixes, query, query result" //
					+ LS + "stdin: query file on local file system (files over a network might be truncated)" //
					+ LS + "arguments: rdf files with extension rdf, ttl, n3 or nt" + LS);
		}
		final Model model = createModel(args);
		final String fullQuery = addNameSpacePrefixes(readQuery(), model);
		System.out.print(fullQuery);
		runQuery(model, fullQuery);
	}

	private static String addNameSpacePrefixes(final String query, final Model model) throws IOException, FileNotFoundException, UnsupportedEncodingException {

		return new ReportRdf().assembleQuery(query, model);
	}

	private static Model createModel(final String[] range) throws FileNotFoundException {

		final Model model = ModelFactory.createDefaultModel();
		for (final String fileName : range) {
			final String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
			final String language = Extension.valueOf(ext).getLanguage();
			model.read(new FileInputStream(fileName), (String) null, language);
		}
		return model;
	}

	private static void runQuery(final Model model, final String query) throws IOException {

		final QueryExecution queryExecution = QueryExecutionFactory.create(query, Syntax.syntaxARQ, model, new QuerySolutionMap());
		final Prologue prologue = new Prologue(PrefixMapping.Factory.create().setNsPrefixes( model.getNsPrefixMap()));
		System.out.write(ResultSetFormatter.asText(queryExecution.execSelect(),prologue).getBytes());
		//System.out.write(ResultSetFormatter.asXMLString(queryExecution.execSelect(),ReportRdf.DEFAULT_STYLE_SHEET).getBytes());
	}

	private static String readQuery() throws IOException, FileNotFoundException {

		final byte[] buffer = new byte[System.in.available()];
		System.in.read(buffer, 0, System.in.available());
		return new String(buffer);
	}
}
