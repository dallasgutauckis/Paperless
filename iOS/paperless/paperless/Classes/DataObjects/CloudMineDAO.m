//
//  CloudMineDAO.m
//  paperless
//
//  Created by Matt Smollinger on 11/4/12.
//  Copyright (c) 2012 Skaffl. All rights reserved.
//

#import "CloudMineDAO.h"

@interface CloudMineDAO()

@property (nonatomic, strong) CMStore *dataStore;

@end

@implementation CloudMineDAO

- (id)init
{
    self = [super init];
    if (self != nil)
    {
        self.dataStore = [CMStore defaultStore];
    }
    return self;
}

- (void)storeImageFile:(UIImage*)image withName:(NSString*)fileName
{
    // assume we have a file, "kitten.jpg". grab the contents
    NSData *imageData =  UIImagePNGRepresentation(image);
    CMStore *store = [CMStore defaultStore];
    
    [store saveFileWithData:imageData
                      named:fileName
          additionalOptions:nil
                   callback:^(CMFileUploadResponse *response) {
                       switch(response.result) {
                           case CMFileCreated:
                               // the file was created, do something with it
                               NSLog(@"File Created");
                               break;
                           case CMFileUpdated:
                               // the file was updated, do something with it
                               NSLog(@"File Updated");
                               break;
                           case CMFileUploadFailed:
                               // upload failed!
                               NSLog(@"Upload Failed");
                               break;
                       }
                   }
     ];
}

- (void)beginFileRetrieval:(NSString*)fileName
{
    [self.dataStore fileWithName:fileName additionalOptions:nil callback:^(CMFileFetchResponse *response)
    {
        if ([self.delegate respondsToSelector:@selector(dataFetchCompletedWithResource:)])
        {
            [self.delegate performSelector:@selector(dataFetchCompletedWithResource:) withObject:response];
        }
        
        // do something with the data..
    }];
}

@end
