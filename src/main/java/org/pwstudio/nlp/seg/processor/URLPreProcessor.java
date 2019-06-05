package org.pwstudio.nlp.seg.processor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.pwstudio.nlp.corpus.tag.Nature;
import org.pwstudio.nlp.dictionary.CoreDictionary;
import org.pwstudio.nlp.dictionary.CoreDictionary.Attribute;
import org.pwstudio.nlp.dictionary.LanguageModel;
import org.pwstudio.nlp.seg.Config;
import org.pwstudio.nlp.seg.common.Vertex;
import org.pwstudio.nlp.seg.common.WordNet;
import org.pwstudio.nlp.utility.Predefine;

public class URLPreProcessor {
		
	public static class UrlPrePocessor extends PreProcessor {

		final Pattern urlPattern = Pattern.compile(
				"(([Hh][Tt]|[Ff])[Tt][Pp][Ss]?):\\/\\/[\\w\\-]+(\\.[\\w\\-]+)+([\\w\\-\\.,@?^=%&:\\/~\\+#]*[\\w\\-\\@?^=%&\\/~\\+#])?");	
		
		public boolean isActive(Config config) {
			return config.mergeLetterAndNumbers;
		}

		@Override
		protected Nature getNature() {
			return Nature.xu;
		}
		
		@Override
		public void recognizeWord(char[] sentence, WordNet wordNetAll) {
			String original = new String(sentence);
			Matcher matcher = urlPattern.matcher(original);
			while (matcher.find()) {
				wordNetAll.add(matcher.start(0)+1, new Vertex(Predefine.TAG_CLUSTER, matcher.group(0), 
						new Attribute(Nature.xu, 10), CoreDictionary.X_WORD_ID));
			}
		}
	};
	
	public static UrlPrePocessor urlProcessor = new UrlPrePocessor();
	
	public static class EmailPreProcessor extends PreProcessor {
		final Pattern urlPattern = Pattern.compile(
				"([\\w_-]+)*@(\\w+[-_]?\\w+[\\.])+[A-Za-z]{2,3}");

		public boolean isActive(Config config) {
			//return false; 
			return config.mergeLetterAndNumbers;
		}

		@Override
		protected Nature getNature() {
			return Nature.xu;
		}
		
		@Override
		public void recognizeWord(char[] sentence, WordNet wordNetAll) {
			String original = new String(sentence);
			Matcher matcher = urlPattern.matcher(original);
			while (matcher.find()) {
				wordNetAll.add(matcher.start(0)+1, new Vertex(Predefine.TAG_CLUSTER, matcher.group(0), 
						new Attribute(Nature.xu, 10), CoreDictionary.X_WORD_ID));
			}
		}

	};

	public static EmailPreProcessor emailProcessor = new EmailPreProcessor();
}
