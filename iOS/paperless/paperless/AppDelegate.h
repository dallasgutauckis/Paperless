//
//  AppDelegate.h
//  paperless
//
//  Created by Matt Smollinger on 11/3/12.
//  Copyright (c) 2012 Skaffl. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CloudMine/CloudMine.h>

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;
@property (nonatomic, unsafe_unretained, readonly) BOOL dropboxLinked;

@end
