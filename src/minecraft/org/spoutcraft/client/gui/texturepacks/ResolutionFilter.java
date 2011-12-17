package org.spoutcraft.client.gui.texturepacks;

import java.util.ArrayList;
import java.util.List;

import org.spoutcraft.client.SpoutClient;
import org.spoutcraft.client.gui.database.UrlElement;
import org.spoutcraft.spoutcraftapi.gui.GenericComboBox;

public class ResolutionFilter extends GenericComboBox implements UrlElement {
	
	TexturePacksDatabaseModel model = SpoutClient.getInstance().getTexturePacksDatabaseModel();
	
	private int possibilities[] = {
			8, 16, 32, 64, 128, 256, 512, 1024	
	};
	
	public ResolutionFilter() {
		List<String> list = new ArrayList<String>();
		list.add("All");
		for(int r:possibilities) {
			list.add(r+"x"+r);
		}
		setItems(list);
	}
	
	public boolean isActive() {
		return getSelectedRow() != 0;
	}

	public String getUrlPart() {
		return "resolution="+possibilities[getSelectedRow()-1];
	}

	public void clear() {
		setSelection(0);
	}
	
	@Override
	public void onSelectionChanged(int item, String text) {
		model.updateUrl();
	}
	
	@Override
	public String getText() {
		return "Resolution: "+getSelectedItem();
	}

}
