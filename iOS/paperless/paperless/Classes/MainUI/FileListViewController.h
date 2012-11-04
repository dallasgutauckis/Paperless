//
//  FileListViewController.h
//  paperless
//
//  Created by Matt Smollinger on 11/3/12.
//  Copyright (c) 2012 Skaffl. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol FileListDelegate <NSObject>
@required
- (void)didSelectDetailItemAtIndexPath:(NSIndexPath*)indexPath;

@end

@interface FileListViewController : UITableViewController

@property (nonatomic, weak) id<FileListDelegate> delegate;

@end
