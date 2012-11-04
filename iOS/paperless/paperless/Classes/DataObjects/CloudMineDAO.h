//
//  CloudMineDAO.h
//  paperless
//
//  Created by Matt Smollinger on 11/4/12.
//  Copyright (c) 2012 Skaffl. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CloudMine/CloudMine.h>

@protocol CloudMineDAODelegate <NSObject>
@optional
- (void)dataFetchCompletedWithResource:(CMFileFetchResponse*)response;
- (void)dataUploadSucceededWithResponse:(CMFileUploadResponse*)response;

@end

@interface CloudMineDAO : NSObject

@property (nonatomic, weak) id<CloudMineDAODelegate>delegate;

- (void)storeImageFile:(UIImage*)image withName:(NSString*)fileName;

- (void)beginFileRetrieval:(NSString*)fileName;

@end
