package tspLib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Splitter;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import tspIps.TspIpInstance;

public class TspLibParser {
	
	public static Map<String,Integer> problemNameToSolution;
	static{
		problemNameToSolution = new HashMap<String,Integer>();
		problemNameToSolution.put("eil51", 426);
		problemNameToSolution.put("bier127", 118282);
		
	}
	
	private static String dir = "sampleData" + File.separator + "TSPLIB" + File.separator;
	private static String dataSuffix = ".tsp";
	private static String solutionSuffix = ".opt.tour";
	private static EnumMap<ParseEdgeWeightType,Metric> metrics;
	static
	{
		metrics = new EnumMap<ParseEdgeWeightType,Metric>(ParseEdgeWeightType.class);
		metrics.put(ParseEdgeWeightType.euc2d, Euclidean.instance);
	}
	
	 private static final Splitter edgeDataSplitter = Splitter.on(' ').trimResults().omitEmptyStrings();
	
	
	private enum DataSectionKeyWords{
		 nodeCoordSection("NODE_COORD_SECTION"), eof("EOF");
		 
		 private String parseAs;
			
			private DataSectionKeyWords(String parseAs){
				this.parseAs = parseAs;
			}
			
			public String getParseAs(){
				return this.parseAs;
			}
			
			public static DataSectionKeyWords parse(String s){
				for(DataSectionKeyWords keyword: DataSectionKeyWords.values()){
					if(keyword.getParseAs().equals(s)){
						return keyword;
					}
				}
				throw new RuntimeException("Could not find \"" + s + "\" as a keyword");
			}
		 
		 
	}
	
	private enum ParseKeyWords{
		name("NAME"), comment("COMMENT"), type("TYPE"), dimension("DIMENSION"),
		edgeWeightType("EDGE_WEIGHT_TYPE"), ;
		
		private String parseAs;
		
		private ParseKeyWords(String parseAs){
			this.parseAs = parseAs;
		}
		
		public String getParseAs(){
			return this.parseAs;
		}
		
		public static ParseKeyWords parse(String s){
			for(ParseKeyWords keyword: ParseKeyWords.values()){
				if(keyword.getParseAs().equals(s)){
					return keyword;
				}
			}
			throw new RuntimeException("Could not find \"" + s + "\" as a keyword");
		}
	}
	
	private enum ParseType{
		tsp("TSP"), atsp("ATSP"), sop("SOP"), hcp("HCP"), cvrp("CVRP"), tour("TOUR");
		
		
		private String parseAs;
		
		private ParseType(String parseAs){
			this.parseAs = parseAs;
		}
		
		public String getParseAs(){
			return this.parseAs;
		}
		
		public static ParseType parse(String s){
			for(ParseType keyword: ParseType.values()){
				if(keyword.getParseAs().equals(s)){
					return keyword;
				}
			}
			throw new RuntimeException("Could not find \"" + s + "\" as a type");
		}
	}
	
	private enum ParseEdgeWeightType{
		
		euc2d("EUC_2D");
		
		
		private String parseAs;
		
		private ParseEdgeWeightType(String parseAs){
			this.parseAs = parseAs;
		}
		
		public String getParseAs(){
			return this.parseAs;
		}
		
		public static ParseEdgeWeightType parse(String s){
			for(ParseEdgeWeightType keyword: ParseEdgeWeightType.values()){
				if(keyword.getParseAs().equals(s)){
					return keyword;
				}
			}
			throw new RuntimeException("Could not find \"" + s + "\" as a edge weight type");
		}
	}
	
	
	
	public static class UnsupportedFileTypeException extends Exception{
		
		
		public UnsupportedFileTypeException(String faultyLine){
			super(faultyLine);
		}
	}
	
	public static TspLibInstance<GeoNode> parse(String fileName) throws UnsupportedFileTypeException{
		String qualifiedFileName = dir+ fileName + dataSuffix; 
		UndirectedGraph<GeoNode,WeightedEdge> graph = new UndirectedSparseGraph<GeoNode,WeightedEdge>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(qualifiedFileName));
			String nextLine = reader.readLine();
			String name = null;
			Metric metric = null;
			while(!nextLine.trim().equals(DataSectionKeyWords.nodeCoordSection.getParseAs())){
				if(nextLine.indexOf(":")<1){
					throw new UnsupportedFileTypeException(nextLine);
				}
				String[] labelValue = nextLine.split(":");
				if(labelValue.length != 2){
					throw new UnsupportedFileTypeException(nextLine);
				}
				String label = labelValue[0].trim();
				String value = labelValue[1].trim();
				ParseKeyWords keyword = ParseKeyWords.parse(label);
				if(keyword == ParseKeyWords.name){
					name = value;
				}
				else if(keyword == ParseKeyWords.comment){
					
				}
				else if(keyword == ParseKeyWords.dimension){
					
				}
				else if(keyword == ParseKeyWords.edgeWeightType){
					ParseEdgeWeightType edgeWeightType = ParseEdgeWeightType.parse(value);
					
					if(metrics.containsKey(edgeWeightType)){
						metric = metrics.get(edgeWeightType);
					}
					else{
						throw new UnsupportedFileTypeException(nextLine);
					}
					
				}
				else if(keyword == ParseKeyWords.type){
					ParseType type = ParseType.parse(value);
					if(type != ParseType.tsp){
						throw new UnsupportedFileTypeException(nextLine);
					}
				}
				nextLine = reader.readLine();
			}
			if(name == null || metric == null){
				throw new UnsupportedFileTypeException("did not find name or metric");
			}
			nextLine = reader.readLine();
			while( nextLine != null && !nextLine.trim().equals(DataSectionKeyWords.eof.getParseAs())){
				double[] values = new double[3];
				Iterator<String> split = TspLibParser.edgeDataSplitter.split(nextLine).iterator();
				for(int i = 0; i < 3; i++){
					if(!split.hasNext()){
						throw new UnsupportedFileTypeException(nextLine);
					}
					try{
						values[i]= Double.parseDouble(split.next());
					}
					catch(NumberFormatException e){
						throw new RuntimeException(nextLine);
					}
				}
				GeoNode node = new GeoNode((int)values[0], values[1], values[2]);
				graph.addVertex(node);
				nextLine = reader.readLine();
			}
			for(GeoNode node1: graph.getVertices()){
				for(GeoNode node2: graph.getVertices()){
					if(node1.getValue() < node2.getValue()){
						graph.addEdge(new WeightedEdge(metric.distance(node1, node2)),node1,node2);
					}
				}
			}
			return new TspLibInstance<GeoNode>(graph,WeightedEdgeTransformer.instance,name);
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	

}
