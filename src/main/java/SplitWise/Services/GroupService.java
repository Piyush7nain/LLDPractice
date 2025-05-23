package SplitWise.Services;

import SplitWise.Models.Group;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GroupService {

    private final List<Group> groups;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();
    public GroupService(List<Group> groups) {
        this.groups = groups;
    }
}
