import { NotificationsService } from './notifications.service';
import { Notification, NotificationType } from './entities/notification.entity';
import { Repository } from 'typeorm';

describe('NotificationsService', () => {
  let service: NotificationsService;
  let notifRepo: Partial<Repository<Notification>>;

  beforeEach(() => {
    notifRepo = {
      findAndCount: jest.fn(),
      count: jest.fn(),
      findOne: jest.fn(),
      save: jest.fn().mockImplementation((n) => Promise.resolve(n)),
    };
    service = new NotificationsService(notifRepo as Repository<Notification>);
  });

  it('should return notifications list with unreadCount', async () => {
    const mockNotif: Notification = {
      id: 'notif-1',
      memberId: 'mem-123',
      title: 'Payment Credited',
      message: 'Your monthly contribution of 500 ETB has been confirmed.',
      type: NotificationType.PAYMENT,
      isRead: false,
      readAt: null,
      metadata: null,
      createdAt: new Date(),
    };

    (notifRepo.findAndCount as jest.Mock).mockResolvedValue([[mockNotif], 1]);
    (notifRepo.count as jest.Mock).mockResolvedValue(1);

    const result = await service.getNotifications('mem-123', 1, 20);
    expect(result.data).toHaveLength(1);
    expect(result.unreadCount).toBe(1);
    expect(result.data[0].type).toBe(NotificationType.PAYMENT);
  });

  it('should mark notification as read', async () => {
    const mockNotif: Notification = {
      id: 'notif-1',
      memberId: 'mem-123',
      title: 'KYC Document Received',
      message: 'Your document is under review.',
      type: NotificationType.KYC,
      isRead: false,
      readAt: null,
      metadata: null,
      createdAt: new Date(),
    };

    (notifRepo.findOne as jest.Mock).mockResolvedValue(mockNotif);

    const result = await service.markAsRead('notif-1', 'mem-123');
    expect(result.message).toBe('Marked as read');
    expect(mockNotif.isRead).toBe(true);
    expect(mockNotif.readAt).toBeDefined();
    expect(notifRepo.save).toHaveBeenCalledWith(mockNotif);
  });
});
