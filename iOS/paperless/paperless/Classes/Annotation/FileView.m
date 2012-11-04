//
//  FileView.m
//  paperless
//
//  Created by Matt Smollinger on 11/3/12.
//  Copyright (c) 2012 Skaffl. All rights reserved.
//

#import "FileView.h"
#import "SmoothLineView.h"

@interface FileView ()

@property (nonatomic, strong) UIScrollView *scrollView;

@property (nonatomic, strong) UIView *scrollingView;

@end

@implementation FileView

@synthesize backgroundImageView = backgroundImageView_;

- (void)viewDidLoad
{
    self.scrollingView = [[UIView alloc] initWithFrame:self.backgroundImageView.bounds];
    SmoothLineView *smooth = [[SmoothLineView alloc] initWithFrame:self.backgroundImageView.bounds];
    smooth.opaque = NO;
    [self.scrollingView addSubview:self.backgroundImageView];
    [self.scrollingView addSubview:smooth];
    
    
    self.scrollView = [[UIScrollView alloc] initWithFrame:self.view.bounds];
    self.scrollView.delegate = self;
    [self.scrollView setMinimumZoomScale:1.0];
    [self.scrollView setMaximumZoomScale:6.0];
    self.scrollView.clipsToBounds = YES;
    self.scrollView.contentSize = self.backgroundImageView.bounds.size;
    [self.scrollView addSubview:self.scrollingView];
    self.scrollView.scrollEnabled = NO;
    
    [self.view addSubview:self.scrollView];
}

- (UIView *)viewForZoomingInScrollView:(UIScrollView *)scrollView
{
    return self.scrollingView;
}

@end
