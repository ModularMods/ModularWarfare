package com.modularwarfare.client.killchat;

import com.google.common.collect.Sets;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class KillFeedManager {
    private Set<KillFeedEntry> entries;

    public KillFeedManager() {
        this.entries = Sets.newConcurrentHashSet();
    }

    public Set<KillFeedEntry> getEntries() {
        return new HashSet<KillFeedEntry>(this.entries);
    }

    public void remove(final KillFeedEntry entry) {
        this.entries.remove(entry);
    }

    public void add(final KillFeedEntry entry) {
        if (this.entries.size() >= 6) {
            this.entries.stream().max(Comparator.comparingInt(KillFeedEntry::getTimeLived)).ifPresent(value -> this.entries.remove(value));
        }
        this.entries.add(entry);
    }
}
