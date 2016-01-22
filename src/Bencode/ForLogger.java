package Bencode;

public class ForLogger {
	
	public static boolean visibility(String niveau, int chiffre)  {
		
		boolean result = false ;
		
		
		
		switch(chiffre) {
		
		case 1:
			if(niveau.equals("nothing"))
				return result;
			break;
			
		case 2:
			if(niveau.equals("nothing") || niveau.equals("info"))
				return result;
			break;
		
		case 3:
			if(niveau.equals("nothing") || niveau.equals("info") || niveau.equals("verbose"))
				return result;
			break;
		
		}
		
		return true;
	}

}
