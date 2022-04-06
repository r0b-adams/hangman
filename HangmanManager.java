// The HangmanManager maintains basic information for a game of Hangman, including
// the letters that have been guessed previously, the number of remaining guesses
// that can be incorrect before a game is over, displaying the correctly guessed and
// still hidden letters of the word being guessed, and keeping a set of words of a given
// length to be guessed. For a given guess, the set of words being considered are separated
// into groups according to where the guessed letter appears in the word. The set of words
// being considered is then narrowed to the largest of these groups. If the guessed letter
// appears in the largest family, then the pattern displaying previous correctly guessed letters
// will be updated with the guessed letter. If not, the number of remaining guesses is decremented

import java.util.*;

public class HangmanManager {
   private Set<String> words;             // current set of words being considered
   private SortedSet<Character> guessed;  // letters guessed in alphabetical order
   private int remainingGuesses;          // number of guesses left
   private String pattern;                // String representing correctly guessed letters of word

   // Param: dictionary: Passed list of words that can be used
   //            length: Length of the words to be considered
   //               max: Number of incorrect guesses before game is over
   //   Pre: length > 1; max >= 0
   //           else throws IllegalArgumentException
   //  Post: Constructs a new HangmanManager, with all words in the initial
   //        set of considered words equal in length to given length
   public HangmanManager(List<String> dictionary, int length, int max) {
      if (length < 1 || max < 0) {
         throw new IllegalArgumentException();
      }
      this.words = new TreeSet<String>();
      this.guessed = new TreeSet<Character>();
      this.remainingGuesses = max;

      for (String s : dictionary) {
         if (s.length() == length) {
            words.add(s);
         }
      }
      String initialPattern = "";
      for (int i = 0; i < length; i++) {
         initialPattern += "- ";
      }
      this.pattern = initialPattern.trim();
   }

   //  Post: Returns an alphbetized set of the words currently being considered
   public Set<String> words() {
      return this.words;
   }

   //  Post: Returns the number of guesses remaining
   public int guessesLeft() {
      return this.remainingGuesses;
   }

   //  Post: Returns an alphabetized set of letters that have been guessed
   public SortedSet<Character> guesses() {
      return this.guessed;
   }

   //   Pre: Throws IllegalStateException if the current set of words is empty
   //  Post: Returns a String representing correctly guessed letters, with hidden
   //        letters represented by dashes and all characters separated by spaces
   public String pattern() {
      if (this.words.isEmpty()) {
         throw new IllegalStateException();
      }
      return this.pattern;
   }

   // Param: guess: guessed letter
   //   Pre: Throws IllegalStateException if remaining number of guesses is
   //           less than one or the set of words being considered is empty
   //        Throws IllegalArgumentException if set of words being considered
   //           is not empty and the guessed letter was already guessed
   //  Post: Records guessed letter into set of previously guessed letter.
   //        Separates set of considered words into groups associated by patterns representing
   //        where the guessed letter appears in word, and chooses the largest of these groups
   //        to be new set of considered words. In case of a tie, chooses the largest of the groups
   //        that occurs first by alphabetical order of the group patterns. Updates the display
   //        pattern to match that of the largest group, including the location of the guessed
   //        character if it appears in the group pattern. If the guessed letter does appear in
   //        the new pattern, returns the number of times the guessed letter occurs. Otherwise,
   //        returns 0 and decrements the number of remaining guesses.
   public int record(char guess) {
      if (this.remainingGuesses < 1 || this.words.isEmpty()) {
         throw new IllegalStateException();
      }
      if (!this.words.isEmpty() && this.guessed.contains(guess)) {
         throw new IllegalArgumentException();
      }
      this.guessed.add(guess);  // records guessed letter

      // separates set of considered words into groups
      Map<String, Set<String>> patternFamily = familiesBuilder(guess);

      // gets the size of the largest group of words
      int max = maxSetSize(patternFamily);

      // removes all groups that have a size less than the largest group
      removeSets(patternFamily, max);

      // of the remaining groups, the first one is selected to be the new set of considered words
      String firstKey = patternFamily.keySet().iterator().next();
      this.pattern = firstKey; // updates display pattern
      this.words = patternFamily.get(firstKey); // updates set of considered words

      // gets number of times guessed letter occurs
      int numChar = numCharOccurence(firstKey, guess);
      if (numChar == 0) {
         this.remainingGuesses--; // decrement remaining guesses if letter does not appear
      }
      return numChar;
   }

   // Param: patternFamily: groups of words associated with a particular
   //                       pattern of letter occurences
   //                  max: Number of words in the largest family
   //  Post: Removes all groups of words that are of a size less than max
   private void removeSets(Map<String, Set<String>> patternFamily, int max) {
      Iterator<String> itr = patternFamily.keySet().iterator();
      while (itr.hasNext()) {
         String groupPattern = itr.next();
         if (patternFamily.get(groupPattern).size() < max) {
            itr.remove();
         }
      }
   }

   // Param: patternFamily: groups of words associated with a particular
   //                       pattern of letter occurences
   //  Post: returns the size of the largest family of words
   private int maxSetSize(Map<String, Set<String>> patternFamily) {
      int largestSize = 1;

      for (String s : patternFamily.keySet()) {
         int familySize = patternFamily.get(s).size();
         if (familySize > largestSize) {
            largestSize = familySize;
         }
      }
      return largestSize;
   }

   // Param: currentPattern: String representing pattern that the current
   //                        set of considered words shares
   //                 guess: character to check for number of occurences
   //  Post: returns number of times that given character occurs in currentPattern
   private int numCharOccurence(String currentPattern, char guess) {
      int numChar = 0;

      for (int i = 0; i < currentPattern.length(); i++) {
         if (currentPattern.charAt(i) == guess) {
            numChar++;
         }
      }
      return numChar;
   }

   // Param: guess: Given letter to create group patterns with
   //  Post: Returns Map with Sets of words associated with a particular pattern
   //        representing where the given character appears in the word
   private Map<String, Set<String>> familiesBuilder(char guess) {
      Map<String, Set<String>> families = new TreeMap<String, Set<String>>();

      for (String current : this.words) {
         String buildPattern = patternBuilder(current, guess);

         if (!families.containsKey(buildPattern)) {
            families.put(buildPattern, new TreeSet<String>());
         }
         families.get(buildPattern).add(current);

      }
      return families;
   }

   // Param: current: word to check
   //          guess: letter to check occurences in word for
   //  Post: returns a String representing where the given letter appears in the word,
   //        accounting for previously guessed correct letters
   private String patternBuilder(String current, char guess) {
      String buildPattern = "";
      for (int i = 0; i < current.length(); i++) {
         if (current.charAt(i) == guess) {
            buildPattern += guess + " ";
         } else {
            buildPattern += this.pattern.charAt(i * 2) + " ";
         }
      }
      return buildPattern.trim();
   }
}