//
//  FileView.h
//  paperless
//
//  Created by Matt Smollinger on 11/3/12.
//  Copyright (c) 2012 Skaffl. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SmoothLineView.h"
#import <CloudMine/CloudMine.h>

@interface FileView : UIViewController <UIScrollViewDelegate>

@property (nonatomic, strong) UIImageView *backgroundImageView;
@property (nonatomic, unsafe_unretained) PenMode thePenMode;



@end
