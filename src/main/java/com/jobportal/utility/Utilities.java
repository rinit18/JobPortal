package com.jobportal.utility;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.jobportal.entity.Sequence;
import com.jobportal.exception.JobPortalException;

import org.springframework.context.annotation.Lazy;

@Component
@Lazy(false)
public class Utilities {
	private static MongoOperations mongoOperation;

	@Autowired
	public void setMongoOperation(MongoOperations mongoOperation) {
		Utilities.mongoOperation = mongoOperation;
	}

	public static Long getNextSequenceId(String key) throws JobPortalException {
		Query query = new Query(Criteria.where("_id").is(key));
		Update update = new Update();
		update.inc("seq", 1);
		FindAndModifyOptions options = new FindAndModifyOptions();
		options.returnNew(true).upsert(true);
		Sequence seqId = mongoOperation.findAndModify(query, update, options, Sequence.class);
		if (seqId == null) {
			throw new JobPortalException("Unable to get sequence id for key : " + key);
		}

		return seqId.getSeq();
	}

	public static String generateOTP() {
		StringBuilder otp = new StringBuilder();
		SecureRandom secureRandom = new SecureRandom();
		for (int i = 0; i < 6; i++) {
			otp.append(secureRandom.nextInt(10));
		}
		return otp.toString();
	}

	public static int calculateLevenshteinDistance(String a, String b) {
		a = a.toLowerCase();
		b = b.toLowerCase();
		int[] costs = new int[b.length() + 1];
		for (int j = 0; j < costs.length; j++)
			costs[j] = j;
		for (int i = 1; i <= a.length(); i++) {
			costs[0] = i;
			int nw = i - 1;
			for (int j = 1; j <= b.length(); j++) {
				int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
						a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
				nw = costs[j];
				costs[j] = cj;
			}
		}
		return costs[b.length()];
	}

	public static boolean isFuzzyMatch(String text, String query) {
		if (text == null || query == null || query.trim().isEmpty()) return false;
		if (text.toLowerCase().contains(query.toLowerCase())) return true;
		
		String[] words = text.toLowerCase().split("\\s+");
		String[] queryWords = query.toLowerCase().trim().split("\\s+");
		
		for (String qWord : queryWords) {
			boolean wordMatched = false;
			for (String word : words) {
				if (word.contains(qWord)) {
					wordMatched = true;
					break;
				}
				int maxDist = Math.max(1, qWord.length() / 4); 
				if (calculateLevenshteinDistance(word, qWord) <= maxDist) {
					wordMatched = true;
					break;
				}
				if (qWord.length() > 3 && word.length() > 3) {
					String wordCons = word.replaceAll("[aeiou]", "");
					String qWordCons = qWord.replaceAll("[aeiou]", "");
					if (wordCons.length() > 0 && qWordCons.length() > 0) {
						if (wordCons.contains(qWordCons) || calculateLevenshteinDistance(wordCons, qWordCons) <= 1) {
							wordMatched = true;
							break;
						}
					}
				}
			}
			if (!wordMatched) return false;
		}
		return true;
	}
}
