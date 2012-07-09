package evymind.vapor.core;

import java.util.ArrayList;

public class ServerLocators extends ArrayList<ServerLocator>{

	private static final long serialVersionUID = 1L;

	public int findNextLocator(int index, boolean forLoadBalance) {
		if (isEmpty()) {
			return -1;
		}
		
		int i = index + 1;
		while (true) {
			if (i >= size()) i = 0;
			
			if (i == index) {
				return -1; // We ended up to the first one.
			} else {
				ServerLocator locator = get(i);
				if (locator.isEnabled()) {
					if (forLoadBalance && !locator.isLoadBalancingServer()) {
						continue;
					}
					return i;
				}
			}
		}
	}

}
