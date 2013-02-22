package com.mobilecomputing.src.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BaseFeatures<T> {
    private Map<GestureName, List<T>> theFeatureMap;

    public Set<GestureName> GetGestures() {
        return theFeatureMap.keySet();
    }

    public void InsertNewFeature(GestureName aGestureName, T aFeature) {
        if(!theFeatureMap.containsKey(aGestureName)) {
            theFeatureMap.put(aGestureName, new ArrayList<T>());
        }

        theFeatureMap.get(aGestureName).add(aFeature);
    }
}
